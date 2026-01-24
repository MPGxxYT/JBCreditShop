package me.mortaldev.jbcreditshop.records;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A generic class representing a pair of two elements.
 *
 * @param <F> The type of the first element in the pair.
 * @param <S> The type of the second element in the pair.
 */
public class Pair<F, S> {
  private F first;
  private S second;

  /**
   * This constructor is used by Jackson to create a Pair object from JSON data.
   *
   * @param first The value for the "first" property in the JSON.
   * @param second The value for the "second" property in the JSON.
   */
  @JsonCreator
  public Pair(@JsonProperty("first") F first, @JsonProperty("second") S second) {
    this.first = first;
    this.second = second;
  }

  /**
   * This annotation tells Jackson to use this method to get the value for the "first" property when
   * writing the object to JSON.
   */
  @JsonProperty("first")
  public F first() {
    return first;
  }

  public void first(F first) {
    this.first = first;
  }

  /**
   * This annotation tells Jackson to use this method to get the value for the "second" property
   * when writing the object to JSON.
   */
  @JsonProperty("second")
  public S second() {
    return second;
  }

  public void second(S second) {
    this.second = second;
  }
}
