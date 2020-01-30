/**
 * 
 */
package com.graphql_java_generator.customcalars;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * A proposed Date scalar, that stores dates in String, formatted as: YYYY-MM-DD?<BR/>
 * This Scalar is proposed to be used, for integration testing (checks that the plugin correctly manages Custom Scalars,
 * see samples) and with more documentation to help people to create their own Custom Scalar implementations.
 * 
 * @author EtienneSF
 */
public class GraphQLScalarTypeDate extends GraphQLScalarType {

	/**
	 * As the constructors for the {@link GraphQLScalarType} are marked as deprecated, the way to create Custom Scalars
	 * may change in the future. So this constructor can only be used by the {@link CustomScalars}, that works as a
	 * singleton factory
	 * 
	 * @param name
	 * @param description
	 * @param coercing
	 * @see GraphQLScalarType#GraphQLScalarType(String, String, Coercing)
	 */
	public GraphQLScalarTypeDate() {
		super("Date", "Custom Scalar for Date management. It serializes dates in String, formatted as: YYYY-MM-DD.",
				// Note:
				// String is the way the data is stored in GraphQL queries
				// Date is the type while in the java code, either in the client and in the server
				new Coercing<Date, String>() {

					/**
					 * The date pattern, used when exchanging date with this {@link GraphQLScalarType} from and to the
					 * GrahQL Server
					 */
					final static String DATE_PATTERN = "yyyy-MM-dd";
					SimpleDateFormat formater = new SimpleDateFormat(DATE_PATTERN);

					/**
					 * Called to convert a Java object result of a DataFetcher to a valid runtime value for the scalar
					 * type. <br/>
					 * Note : Throw {@link graphql.schema.CoercingSerializeException} if there is fundamental problem
					 * during serialisation, don't return null to indicate failure. <br/>
					 * Note : You should not allow {@link java.lang.RuntimeException}s to come out of your serialize
					 * method, but rather catch them and fire them as {@link graphql.schema.CoercingSerializeException}
					 * instead as per the method contract.
					 *
					 * @param dataFetcherResult
					 *            is never null
					 *
					 * @return a serialized value which may be null.
					 *
					 * @throws graphql.schema.CoercingSerializeException
					 *             if value input can't be serialized
					 */
					@Override
					public String serialize(Object input) throws CoercingSerializeException {
						if (!(input instanceof Date)) {
							throw new CoercingSerializeException(
									"Can't parse the '" + input.toString() + "' string to a Date");
						} else {
							return formater.format((Date) input);
						}
					}

					/**
					 * Called to resolve an input from a query variable into a Java object acceptable for the scalar
					 * type. <br/>
					 * Note : You should not allow {@link java.lang.RuntimeException}s to come out of your parseValue
					 * method, but rather catch them and fire them as {@link graphql.schema.CoercingParseValueException}
					 * instead as per the method contract.
					 *
					 * @param input
					 *            is never null
					 *
					 * @return a parsed value which is never null
					 *
					 * @throws graphql.schema.CoercingParseValueException
					 *             if value input can't be parsed
					 */
					@Override
					public Date parseValue(Object o) throws CoercingParseValueException {
						if (!(o instanceof String)) {
							throw new CoercingParseValueException(
									"Can't parse the '" + o.toString() + "' string to a Date");
						} else {
							try {
								return formater.parse((String) o);
							} catch (ParseException e) {
								throw new CoercingParseValueException(e.getMessage(), e);
							}
						}
					}

					/**
					 * Called during query validation to convert a query input AST node into a Java object acceptable
					 * for the scalar type. The input object will be an instance of {@link graphql.language.Value}.
					 * <br/>
					 * Note : You should not allow {@link java.lang.RuntimeException}s to come out of your parseLiteral
					 * method, but rather catch them and fire them as
					 * {@link graphql.schema.CoercingParseLiteralException} instead as per the method contract.
					 *
					 * @param input
					 *            is never null
					 *
					 * @return a parsed value which is never null
					 *
					 * @throws graphql.schema.CoercingParseLiteralException
					 *             if input literal can't be parsed
					 */
					@Override
					public Date parseLiteral(Object o) throws CoercingParseLiteralException {
						// o is an AST, that is: an instance of a class that implements graphql.language.Value
						if (!(o instanceof StringValue)) {
							throw new CoercingParseValueException(
									"Can't parse the '" + o.toString() + "' string to a Date");
						} else {
							try {
								return formater.parse(((StringValue) o).getValue());
							} catch (ParseException e) {
								throw new CoercingParseValueException(e.getMessage(), e);
							}
						}
					}
				});
	}

}
