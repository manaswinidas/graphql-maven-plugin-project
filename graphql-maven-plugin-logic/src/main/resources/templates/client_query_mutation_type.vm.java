##
## Velocity template for the Query or Mutation type (client side)
##
## The generated class contains:
## - If separateUtilityClasses is false: all the fields (and their getters/setters), as stated in the GraphQL schema 
## - All the utility classes that allow to prepare and execute the query/mutation
##
##
## This template has these inputs:
## packageUtilName 			The package where this class must be generated
## configuration		The plugin's configuration
## object					The query or mutation type, for which this executor is being generated
##
/** Generated by the default template from graphql-java-generator */
package ${packageUtilName};
#macro(inputParams)#foreach ($inputParameter in $field.inputParameters), ${inputParameter.javaType} ${inputParameter.javaName}#end#end
#macro(inputValues)#foreach ($inputParameter in $field.inputParameters), ${inputParameter.javaName}#end#end

import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.graphql_java_generator.annotation.GraphQLNonScalar;
import com.graphql_java_generator.annotation.GraphQLScalar;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import com.graphql_java_generator.client.GraphQLObjectMapper;
import com.graphql_java_generator.client.request.InputParameter;
import com.graphql_java_generator.client.request.ObjectResponse;

#foreach($import in ${object.imports})
import $import;
#end

#foreach($import in ${object.importsForUtilityClasses})
#if ($import != "${configuration.packageName}.${object.classSimpleName}")
import $import;
#end
#end

import com.graphql_java_generator.client.GraphQLConfiguration;
import com.graphql_java_generator.client.GraphqlClientUtils;

/**
#foreach ($comment in $object.comments)
 * $comment
#end
#if ($object.comments.size() > 0)
 * <BR/>
 * <BR/>
#end
 * This class contains the response for a full request. See the 
 * <A HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/exec_graphql_requests.html">plugin web site</A> 
 * for more information on full and partial requests.<BR/>
 * It also allows access to the _extensions_ part of the response. Take a look at the 
 * <A HRE="https://spec.graphql.org/June2018/#sec-Response">GraphQL spec</A> for more information on this.
 * 
 * @author generated by graphql-java-generator
 * @see <a href="https://github.com/graphql-java-generator/graphql-java-generator">https://github.com/graphql-java-generator/graphql-java-generator</a>
 */
## This class is deprecated when generated in the util package (the XxxExecutor class should be used instead)
## When in the GraphQL main package, then this class is mandatory to retrieve full requests results
#if (${configuration.separateUtilityClasses})
@Deprecated
#end
${object.annotation}
public class ${object.classSimpleName} extends ${object.classSimpleName}Executor #if(!${configuration.separateUtilityClasses} && ${object.requestType})implements com.graphql_java_generator.client.GraphQLRequestObject #end{

#if(!${configuration.separateUtilityClasses})
##
## For objects that represent the requests (query, mutation and subscription), we add the capability to decode the GraphQL extensions response field
##
#if(${object.requestType})
	private GraphQLObjectMapper extensionMapper = null;
	private JsonNode extensions;
	private Map<String, JsonNode> extensionsAsMap = null;

#end
#parse ("templates/object_content.vm.java")
#end

	/** {@inheritDoc} */
	public ${object.classSimpleName}(String graphqlEndpoint) {
		super(graphqlEndpoint);
	}

	/** {@inheritDoc} */
	public ${object.classSimpleName}(String graphqlEndpoint, SSLContext sslContext, HostnameVerifier hostnameVerifier) {
		super(graphqlEndpoint, sslContext, hostnameVerifier);
	}

	/** {@inheritDoc} */
	public ${object.classSimpleName}(String graphqlEndpoint, Client client) {
		super(graphqlEndpoint, client);
	}

##
## For objects that represent the requests (query, mutation and subscription), we add the capability to decode the GraphQL extensions response field
##
#if(!${configuration.separateUtilityClasses} && ${object.requestType})
	private GraphQLObjectMapper getExtensionMapper() {
		if (extensionMapper == null) {
			extensionMapper = new GraphQLObjectMapper("${packageUtilName}", null);
		}
		return extensionMapper;
	}
	
	public JsonNode getExtensions() {
		return extensions;
	}
	
	public void setExtensions(JsonNode extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * Returns the extensions as a map. The values can't be deserialized, as their type is unknown.
	 * 
	 * @return
	 */
	public Map<String, JsonNode> getExtensionsAsMap() {
		if (extensionsAsMap == null) {
			extensionsAsMap = getExtensionMapper().convertValue(extensions, new TypeReference<Map<String, JsonNode>>() {
			});
		}
		return extensionsAsMap;
	}
	
	/**
	 * Parse the value for the given _key_, as found in the <I>extensions</I> field of the GraphQL server's response,
	 * into the given _t_ class.
	 * 
	 * @param <T>
	 * @param key
	 * @param t
	 * @return null if the key is not in the <I>extensions</I> map. Otherwise: the value for this _key_, as a _t_
	 *         instance
	 * @throws JsonProcessingException
	 *             When there is an error when converting the key's value into the _t_ class
	 */
	public <T> T getExtensionsField(String key, Class<T> t) throws JsonProcessingException {
		JsonNode node = getExtensionsAsMap().get(key);
		return (node == null) ? null : getExtensionMapper().treeToValue(node, t);
	}
#end

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public ${object.classSimpleName}#if(${configuration.generateDeprecatedRequestResponse})Response#end execWithBindValues(String queryResponseDef, Map<String, Object> parameters)
			throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		return super.exec(queryResponseDef, parameters);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public ${object.classSimpleName}#if(${configuration.generateDeprecatedRequestResponse})Response#end exec(String queryResponseDef, Object... paramsAndValues)
			throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		return super.exec(queryResponseDef, paramsAndValues);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public ${object.classSimpleName}#if(${configuration.generateDeprecatedRequestResponse})Response#end execWithBindValues(ObjectResponse objectResponse, Map<String, Object> parameters)
			throws GraphQLRequestExecutionException {
		return super.execWithBindValues(objectResponse, parameters);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public ${object.classSimpleName}#if(${configuration.generateDeprecatedRequestResponse})Response#end exec(ObjectResponse objectResponse, Object... paramsAndValues)
			throws GraphQLRequestExecutionException {
		return super.exec(objectResponse, paramsAndValues);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public com.graphql_java_generator.client.request.Builder getResponseBuilder() throws GraphQLRequestPreparationException {
		return super.getResponseBuilder();
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public GraphQLRequest getGraphQLRequest(String fullRequest) throws GraphQLRequestPreparationException {
		return super.getGraphQLRequest(fullRequest);
	}

#foreach ($field in $object.fields)
	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
## Note: we must use the ${query.type.classSimpleName}, as when the GraphQL schema uses request that return the query type, and 
## the query type object is in a separate package (plugin parameter separateUtilityClasses), then there is a conflict between 
## the current name and the query type object: they have the same name, but are in different packages 
#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
	public ${field.javaTypeFullClassname} ${field.name}WithBindValues(String queryResponseDef#inputParams(), Map<String, Object> parameters)
			throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		return super.${field.name}WithBindValues(queryResponseDef#inputValues(), parameters);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
## Note: we must use the ${query.type.classSimpleName}, as when the GraphQL schema uses request that return the query type, and 
## the query type object is in a separate package (plugin parameter separateUtilityClasses), then there is a conflict between 
## the current name and the query type object: they have the same name, but are in different packages 	#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
	public ${field.javaTypeFullClassname} ${field.name}(String queryResponseDef#inputParams(), Object... paramsAndValues)
			throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		return super.${field.name}(queryResponseDef#inputValues(), paramsAndValues);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
## Note: we must use the ${query.type.classSimpleName}, as when the GraphQL schema uses request that return the query type, and 
## the query type object is in a separate package (plugin parameter separateUtilityClasses), then there is a conflict between 
## the current name and the query type object: they have the same name, but are in different packages 	#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
	public ${field.javaTypeFullClassname} ${field.name}WithBindValues(ObjectResponse objectResponse#inputParams(), Map<String, Object> parameters)
			throws GraphQLRequestExecutionException  {
		return super.${field.name}WithBindValues(objectResponse#inputValues(), parameters);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
## Note: we must use the ${query.type.classSimpleName}, as when the GraphQL schema uses request that return the query type, and 
## the query type object is in a separate package (plugin parameter separateUtilityClasses), then there is a conflict between 
## the current name and the query type object: they have the same name, but are in different packages 	#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
#if(${field.type.scalar})	@GraphQLScalar #else	@GraphQLNonScalar #end(fieldName = "${field.name}", graphQLTypeSimpleName = "${field.graphQLTypeSimpleName}", javaClass = ${field.type.classSimpleName}.class)
	public ${field.javaTypeFullClassname} ${field.javaName}(ObjectResponse objectResponse#inputParams(), Object... paramsAndValues)
			throws GraphQLRequestExecutionException  {
		return super.${field.javaName}(objectResponse#inputValues(), paramsAndValues);
	}

	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public com.graphql_java_generator.client.request.Builder get${field.pascalCaseName}ResponseBuilder() throws GraphQLRequestPreparationException {
		return super.get${field.pascalCaseName}ResponseBuilder();
	}


	/**
	 * This method is deprecated: please use {@link ${object.classSimpleName}Executor} class instead of this class, to execute this method. 
	 * It is maintained to keep existing code compatible with the generated code. It will be removed in 2.0 version.
	 */
	@Deprecated
	public GraphQLRequest get${field.pascalCaseName}GraphQLRequest(String partialRequest) throws GraphQLRequestPreparationException {
		return super.get${field.pascalCaseName}GraphQLRequest(partialRequest);
	}
	
#end
}
