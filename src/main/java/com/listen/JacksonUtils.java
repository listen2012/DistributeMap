package com.listen;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonUtils {

  private static ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // mapper.setDateFormat(sdf);
  }

  public static <T> T toObject(String json, Class<T> clazz) throws Exception {
    T result = null;
    try {
      // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
      // false);
      result = objectMapper.readValue(json, clazz);
    } catch (Exception e) {
      throw e;
    }
    return result;
  }

  public static String toString(Object o) throws Exception {
    String json = null;
    try {
      json = objectMapper.writeValueAsString(o);
    } catch (Exception e) {
      throw e;
    }
    return json;
  }

  /**
   * 将对象序列化
   * 
   * @param obj
   * @return
   */
  public static String getJsonFromObject(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 反序列化对象字符串
   * 
   * @param json
   * @param clazz
   * @return
   */
  public static <T> T getObjectFromJson(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 反序列化字符串成为对象
   * 
   * @param json
   * @param valueTypeRef
   * @return
   */
  public static <T> T getObjectFromJson(String json, TypeReference<T> valueTypeRef) {
    try {
      return objectMapper.readValue(json, valueTypeRef);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
