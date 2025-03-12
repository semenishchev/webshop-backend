package cc.olek.webshop;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConstantPool {
    public static final ObjectMapper JSON = new ObjectMapper(new JsonFactory());
}
