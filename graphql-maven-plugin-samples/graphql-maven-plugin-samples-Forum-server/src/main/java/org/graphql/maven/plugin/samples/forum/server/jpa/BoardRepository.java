/**
 * 
 */
package org.graphql.maven.plugin.samples.forum.server.jpa;

import org.graphql.maven.plugin.samples.forum.server.Board;
import org.springframework.data.repository.CrudRepository;

/**
 * @author EtienneSF
 */
public interface BoardRepository extends CrudRepository<Board, String> {

}
