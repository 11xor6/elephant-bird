package com.twitter.elephantbird.pig.store;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.pig.data.Tuple;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * A storage class to store the ouput of each tuple in a delimited file
 * like PigStorage, but LZO compressed.
 */
public class LzoJsonStorage extends LzoBaseStoreFunc {
  private static final Logger LOG = LoggerFactory.getLogger(LzoJsonStorage.class);

  // If null, keep all keys.
  private final Set<String> keysToKeep_;
  private JSONObject json = new JSONObject();

  public LzoJsonStorage() {
    keysToKeep_ = null;
    LOG.info("Initialized LzoJsonStorage. Keeping all keys.");
  }

  public LzoJsonStorage(String...keysToKeep) {
    keysToKeep_ = Sets.newHashSet(keysToKeep);
    LOG.info("Initialized LzoJsonStorage. Keeping keys " + Joiner.on(", ").join(keysToKeep_) + ".");
  }


  /**
   * The first element is expected to be a map, or null. Anything else causes an error.
   * @param tuple the tuple to write.
   */
  @SuppressWarnings("unchecked")
  public void putNext(Tuple tuple) throws IOException {
    json.clear();
    if (tuple != null && tuple.size() >= 1) {
      Map<String, Object> map = (Map<String, Object>) tuple.get(0);
      if (keysToKeep_ == null) {
        json.putAll(map);
      } else {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
          if (keysToKeep_.contains(entry.getKey())) {
            json.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }
    os_.write(json.toString().getBytes("UTF-8"));
    os_.write('\n');
  }

}

