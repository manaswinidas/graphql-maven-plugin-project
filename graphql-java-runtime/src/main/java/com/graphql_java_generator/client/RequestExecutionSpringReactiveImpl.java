/**
 * 
 */
package com.graphql_java_generator.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.graphql_java_generator.annotation.RequestType;
import com.graphql_java_generator.client.request.AbstractGraphQLRequest;
import com.graphql_java_generator.client.response.JsonResponseWrapper;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This is the default implementation for the {@link RequestExecution} This implementation has been added in version
 * 1.12.<BR/>
 * It is loaded by the {@link SpringConfiguration} Spring configuration class, that is generated with the client code.
 * 
 * @since 1.12
 * @author etienne-sf
 */
public class RequestExecutionSpringReactiveImpl implements RequestExecution {

	/** Logger for this class */
	private static Logger logger = LoggerFactory.getLogger(RequestExecutionSpringReactiveImpl.class);

	/**
	 * A <I>graphqlEndpoint</I> Spring bean, of type String, must be provided, with the URL of the GraphQL endpoint, for
	 * instance <I>https://my.serveur.com/graphql</I>
	 */
	String graphqlEndpoint;

	/**
	 * If the subscription is on a different endpoint than the main GraphQL endpoint, thant you can define a
	 * <I>graphqlSubscriptionEndpoint</I> Spring bean, of type String, with this specific URL, for instance
	 * <I>https://my.serveur.com/graphql/subscription</I>. <BR/>
	 * For instance, Java servers suffer from a limitation which prevent to server both GET/POST HTTP verbs and
	 * WebSockets on the same URL. This limitation is now under control, for instance in the server version of this
	 * plugin.<BR/>
	 * If no bean <I>graphqlSubscriptionEndpoint</I> Spring bean is defined, then the <I>graphqlEndpoint</I> URL is also
	 * used for subscriptions (which is the standard case).
	 */
	@Deprecated
	String graphqlSubscriptionEndpoint;

	/**
	 * The optional {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} that manages the OAuth Authorization
	 * header, on client side
	 */
	ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction;

	/**
	 * The optional {@link OAuthTokenExtractor} that extracts the OAuth Authorization header, once the
	 * {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} has gotten it
	 */
	OAuthTokenExtractor oAuthTokenExtractor;

	/**
	 * The Spring reactive {@link WebClient} that will execute the HTTP requests for GraphQL queries and mutations.
	 */
	WebClient webClient;

	/**
	 * The Spring reactive {@link WebSocketClient} web socket client, that will execute HTTP requests to build the web
	 * sockets, for GraphQL subscriptions.<BR/>
	 * This is mandatory if the application latter calls subscription. It may be null otherwise.
	 */
	WebSocketClient webSocketClient;

	/**
	 * This constructor may be called by Spring, once it has build a {@link WebClient} bean, or directly, in non Spring
	 * applications.
	 * 
	 * @param graphqlEndpoint
	 *            A <I>graphqlEndpoint</I> Spring bean, of type String, must be provided, with the URL of the GraphQL
	 *            endpoint, for instance <I>https://my.serveur.com/graphql</I>
	 * @param graphqlSubscriptionEndpoint
	 *            If the subscription is on a different endpoint than the main GraphQL endpoint, thant you can define a
	 *            <I>graphqlSubscriptionEndpoint</I> Spring bean, of type String, with this specific URL, for instance
	 *            <I>https://my.serveur.com/graphql/subscription</I>. For instance, Java servers suffer from a
	 *            limitation which prevent to server both GET/POST HTTP verbs and WebSockets on the same URL.<BR/>
	 *            If no bean <I>graphqlSubscriptionEndpoint</I> Spring bean is defined, then the <I>graphqlEndpoint</I>
	 *            URL is also used for subscriptions (which is the standard case).
	 * @param webClient
	 *            The Spring reactive {@link WebClient} that will execute the HTTP requests for GraphQL queries and
	 *            mutations.
	 * @param webSocketClient
	 *            The Spring reactive {@link WebSocketClient} web socket client, that will execute HTTP requests to
	 *            build the web sockets, for GraphQL subscriptions.<BR/>
	 *            This is mandatory if the application latter calls subscription. It may be null otherwise.
	 * @param serverOAuth2AuthorizedClientExchangeFilterFunction
	 *            The {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} is responsible for getting OAuth token
	 *            from the OAuth authorization server. It is optional, and may be provided by the App's spring config.
	 *            If it is not provided, then there is no OAuth authentication on client side. If provided, then the
	 *            client uses it to provide the OAuth2 authorization token, when accessing the GraphQL resource server
	 *            for queries/mutations/subscriptions.
	 * @param oAuthTokenExtractor
	 *            This class is responsible for extracting the OAuth token, once the
	 *            {@link ServerOAuth2AuthorizedClientExchangeFilterFunction} has done its job, and added the OAuth2
	 *            token into the request, in the Authorization header. See the {@link OAuthTokenExtractor} doc for more
	 *            information.
	 */
	@Autowired
	public RequestExecutionSpringReactiveImpl(String graphqlEndpoint, //
			String graphqlSubscriptionEndpoint, //
			WebClient webClient, //
			WebSocketClient webSocketClient,
			ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction,
			OAuthTokenExtractor oAuthTokenExtractor) {
		this.graphqlEndpoint = graphqlEndpoint;
		this.graphqlSubscriptionEndpoint = graphqlSubscriptionEndpoint;
		this.webClient = webClient;
		this.webSocketClient = webSocketClient;
		this.serverOAuth2AuthorizedClientExchangeFilterFunction = serverOAuth2AuthorizedClientExchangeFilterFunction;
		this.oAuthTokenExtractor = oAuthTokenExtractor;

		// The reactive framework needs to be started, before the first request is executed. We start it now, to reduce
		// the latency of the first GraphQL request to come.
		// We do this in a separate thread, to let the initialization process go on
		new Thread() {
			@Override
			public void run() {
				// Let's create a dummy Mono to start the reactive framework as soon as we know that the reactive
				// framework is needed
				Mono.just(true).subscribe();
			}
		}.start();
	}

	@Override
	public <R extends GraphQLRequestObject> R execute(AbstractGraphQLRequest graphQLRequest,
			Map<String, Object> parameters, Class<R> dataResponseType) throws GraphQLRequestExecutionException {

		if (graphQLRequest.getRequestType().equals(RequestType.subscription))
			throw new GraphQLRequestExecutionException("This method may not be called for subscriptions");

		String jsonRequest = graphQLRequest.buildRequest(parameters);

		try {

			logger.trace(GRAPHQL_MARKER, "Executing GraphQL request: {}", jsonRequest);

			JsonResponseWrapper responseJson = webClient//
					.post()//
					.contentType(MediaType.APPLICATION_JSON)//
					.body(Mono.just(jsonRequest), String.class)//
					.accept(MediaType.APPLICATION_JSON)//
					.retrieve()//
					.bodyToMono(JsonResponseWrapper.class)//
					.block();

			return parseDataFromGraphQLServerResponse(graphQLRequest.getGraphQLObjectMapper(), responseJson,
					dataResponseType);

		} catch (IOException e) {
			throw new GraphQLRequestExecutionException(
					"Error when executing query <" + jsonRequest + ">: " + e.getMessage(), e);
		}
	}

	@Override
	public <R, T> SubscriptionClient execute(AbstractGraphQLRequest graphQLRequest, Map<String, Object> parameters,
			SubscriptionCallback<T> subscriptionCallback, Class<R> subscriptionType, Class<T> messageType)
			throws GraphQLRequestExecutionException {

		// This method accepts only subscription at a time (no query and no mutation)
		if (!graphQLRequest.getRequestType().equals(RequestType.subscription))
			throw new GraphQLRequestExecutionException("This method may be called only for subscriptions");

		// Subscription may be subscribed only once at a time, as this method allows only one subscriptionCallback
		if (graphQLRequest.getSubscription().getFields().size() != 1) {
			throw new GraphQLRequestExecutionException(
					"This method may be called only for one subscription at a time, but there was "
							+ graphQLRequest.getSubscription().getFields().size()
							+ " subscriptions in this GraphQLRequest");
		}

		String subscriptionName = graphQLRequest.getSubscription().getFields().get(0).getName();

		// Is there an OAuth authentication to handle?
		HttpHeaders headers = new HttpHeaders();
		if (serverOAuth2AuthorizedClientExchangeFilterFunction != null && oAuthTokenExtractor != null) {
			String authorizationHeaderValue = oAuthTokenExtractor.getAuthorizationHeaderValue();
			logger.debug("Got this OAuth token (authorization header value): {}", authorizationHeaderValue);
			headers.add(OAuthTokenExtractor.AUTHORIZATION_HEADER_NAME, authorizationHeaderValue);
		} else {
			logger.debug(
					"No serverOAuth2AuthorizedClientExchangeFilterFunction or no oAuthTokenExtractor where provided. No OAuth token is provided.");
		}

		String request = graphQLRequest.buildRequest(parameters);
		logger.debug(GRAPHQL_MARKER, "Executing GraphQL subscription '{}' with request {}", subscriptionName, request);

		// Let's create and start the Web Socket
		GraphQLReactiveWebSocketHandler<R, T> webSocketHandler = new GraphQLReactiveWebSocketHandler<>(request,
				subscriptionName, subscriptionCallback, subscriptionType, messageType,
				graphQLRequest.getGraphQLObjectMapper());
		logger.trace(GRAPHQL_MARKER, "Before execution of GraphQL subscription '{}' with request {}", subscriptionName,
				request);
		Disposable disposable = webSocketClient.execute(getWebSocketURI(), headers, webSocketHandler)
				.subscribeOn(Schedulers.single())// Let's have a dedicated thread
				.subscribe();
		logger.trace(GRAPHQL_MARKER, "After execution of GraphQL subscription '{}' with request {}", subscriptionName,
				request);

		return new SubscriptionClientReactiveImpl(disposable, webSocketHandler.getSession());
	}

	/**
	 * Retrieves the URI for the Web Socket, based on the GraphQL endpoint that has been given to the Constructor
	 * 
	 * @return
	 * @throws GraphQLRequestExecutionException
	 */
	public URI getWebSocketURI() throws GraphQLRequestExecutionException {
		String endpoint = (graphqlSubscriptionEndpoint != null) ? graphqlSubscriptionEndpoint : graphqlEndpoint;
		if (endpoint.startsWith("http:") || endpoint.startsWith("https:")) {
			// We'll use the ws or the wss protocol. Let's just replace http by ws for that
			try {
				return new URI("ws" + endpoint.substring(4));
			} catch (URISyntaxException e) {
				throw new GraphQLRequestExecutionException(
						"Error when trying to determine the Web Socket endpoint for GraphQL endpoint " + endpoint, e);
			}
		}
		throw new GraphQLRequestExecutionException(
				"non managed protocol for endpoint " + endpoint + ". This method manages only http and https");
	}

	/**
	 * Extract the data from the {@link JsonResponseWrapper#data} json node, and return it as a T instance.
	 * 
	 * @param <T>
	 * @param response
	 *            The json response, read from the GraphQL response
	 * @param dataResponseType
	 *            The expected T class
	 * @return
	 * @throws JsonProcessingException
	 * @throws GraphQLRequestExecutionException
	 */
	static <T extends GraphQLRequestObject> T parseDataFromGraphQLServerResponse(GraphQLObjectMapper objectMapper2,
			JsonResponseWrapper response, Class<T> valueType)
			throws GraphQLRequestExecutionException, JsonProcessingException {
		if (logger.isTraceEnabled()) {
			logger.trace("Response data: {}", objectMapper2.writeValueAsString(response.data));
			logger.trace("Response errors: {}", objectMapper2.writeValueAsString(response.errors));
		}

		if (response.errors == null || response.errors.size() == 0) {
			// No errors. Let's parse the data
			T ret = objectMapper2.treeToValue(response.data, valueType);
			ret.setExtensions(response.extensions);
			return ret;
		} else {
			int nbErrors = 0;
			String agregatedMessage = null;
			for (com.graphql_java_generator.client.response.Error error : response.errors) {
				String msg = error.toString();
				nbErrors += 1;
				logger.error(GRAPHQL_MARKER, msg);
				if (agregatedMessage == null) {
					agregatedMessage = msg;
				} else {
					agregatedMessage += ", ";
					agregatedMessage += msg;
				}
			}
			if (nbErrors == 0) {
				throw new GraphQLRequestExecutionException("An unknown error occured");
			} else {
				throw new GraphQLRequestExecutionException(nbErrors + " errors occured: " + agregatedMessage);
			}
		}
	}
}
