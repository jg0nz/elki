package de.lmu.ifi.dbs.algorithm.result;

import de.lmu.ifi.dbs.algorithm.SLINK;
import de.lmu.ifi.dbs.data.MetricalObject;
import de.lmu.ifi.dbs.database.Database;
import de.lmu.ifi.dbs.distance.Distance;
import de.lmu.ifi.dbs.distance.DistanceFunction;
import de.lmu.ifi.dbs.normalization.NonNumericFeaturesException;
import de.lmu.ifi.dbs.normalization.Normalization;
import de.lmu.ifi.dbs.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides the result of the single link algorithm SLINK.
 *
 * @author Elke Achtert (<a
 *         href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public class PointerRepresentation<O extends MetricalObject, D extends Distance, DF extends DistanceFunction<O,D>>
extends AbstractResult<O> {

  /**
   * The values of the function Pi of the pointer representation.
   */
  private HashMap<Integer, Integer> pi = new HashMap<Integer, Integer>();

  /**
   * The values of the function Lambda of the pointer representation.
   */
  private HashMap<Integer, SLINK<O,D,DF>.SLinkDistance> lambda = new HashMap<Integer, SLINK<O,D,DF>.SLinkDistance>();

  /**
   * The distance function this pointer representation was computed with.
   */
  private DistanceFunction<O,D> distanceFunction;

  /**
   * Creates a new pointer representation.
   *
   * @param pi               the values of the function Pi of the pointer representation
   * @param lambda           the values of the function Lambda of the pointer
   *                         representation
   * @param distanceFunction the distance function this pointer representation was computed
   *                         with
   * @param database         the database containing the objects
   */
  public PointerRepresentation(HashMap<Integer, Integer> pi,
                               HashMap<Integer, SLINK<O,D,DF>.SLinkDistance> lambda,
                               DistanceFunction<O,D> distanceFunction,
                               Database<O> database
  ) {
    super(database);
    this.pi = pi;
    this.lambda = lambda;
    this.distanceFunction = distanceFunction;
  }

  /**
   * @see Result#output(java.io.File, de.lmu.ifi.dbs.normalization.Normalization,
   * java.util.List<de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings>)
   */
  public void output(File out, Normalization<O> normalization, List<AttributeSettings> settings) throws UnableToComplyException {
    PrintStream outStream;
    try {
      outStream = new PrintStream(new FileOutputStream(out));
    }
    catch (Exception e) {
      outStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
    }

    try {
      writeHeader(outStream, settings);
    }
    catch (NonNumericFeaturesException e) {
      throw new UnableToComplyException(e);
    }

    outStream.println(this.toString());
    outStream.flush();
  }

  /**
   * Returns a string representation of this pointer representation.
   *
   * @return a string representation of this pointer representation
   */
  public String toString() {
    StringBuffer result = new StringBuffer();

    SortedSet<Integer> keys = new TreeSet<Integer>(pi.keySet());
    for (Integer id : keys) {
      result.append("P(");
      result.append(id);
      result.append(") = ");
      result.append(pi.get(id));
      result.append("   L(");
      result.append(id);
      result.append(") = ");
      result.append(lambda.get(id));
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * Returns the clustering result for a given distance threshold.
   *
   * @param distancePattern the pattern of the threshold
   * @return the clustering result: each element of the returned collection is
   *         a list of ids representing one cluster
   */
  public Collection<List<Integer>> getClusters(String distancePattern) {
    Distance distance = distanceFunction.valueOf(distancePattern);

    HashMap<Integer, List<Integer>> partitions = new HashMap<Integer, List<Integer>>();
    for (Integer id : pi.keySet()) {
      Integer partitionID = id;
      while (lambda.get(partitionID).getDistance().compareTo(distance) <= 0) {
        partitionID = pi.get(partitionID);
      }

      List<Integer> partition = partitions.get(partitionID);
      if (partition == null) {
        partition = new ArrayList<Integer>();
        partitions.put(partitionID, partition);
      }
      partition.add(id);
    }
    return partitions.values();
  }
}
