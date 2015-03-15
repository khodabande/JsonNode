import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a json node. A json node is simply a json value
 * which could be of type OBJECT, ARRAY, NUMBER, STRING, BOOLEAN, NULL.
 * <p>
 * A json string could be parsed using the static method {@link #parse}
 * which returns a json node.
 * <ul>
 *    <li>
 *        A json <b>object</b> will be parsed into a {@link java.util.HashMap}
 *        of {@link String} and {@link JsonNode}. One can access to this
 *        hash map using {@link #getAsMap} method
 *    </li>
 *    <li>
 *        A json <b>array</b> will be parsed into an {@link java.util.ArrayList}
 *        of {@link JsonNode}. One can access to this array list using
 *        {@link #getAsArray} method.
 *    </li>
 *    <li>
 *        A json <b>number</b> will be parsed into an {@link java.lang.String}.
 *        One can access to this number by method {@link #getAsString} or
 *        {@link #getAsInt}.
 *    </li>
 *    <li>
 *        A json {@link java.lang.String} will be parsed into an {@link java.lang.String}
 *        and could be accessed by method {@link #getAsString}.
 *    </li>
 *    <li>
 *        A json boolean will be parsed into a {@link java.lang.Boolean} using
 *    </li>
 * </ul>
 * <p>
 * To serialize a node use methods {@link #toJson} or {@link #toString}.
 */
public class JsonNode {

    public static enum NodeType {
        OBJECT, ARRAY, NUMBER, STRING, BOOLEAN, NULL, NOT_EXIST
    }

    private Object node;
    private NodeType type;

    public NodeType getType() {
        return type;
    }

    public JsonNode get(String name) {
        if (type != NodeType.OBJECT)
            return NOT_EXIST;
        HashMap<String, JsonNode> object = getAsMap();
        return object.getOrDefault(name, NOT_EXIST);
    }

    public JsonNode add(String name, JsonNode value) {
        if (type != NodeType.OBJECT)
            return this;
        HashMap<String, JsonNode> object = getAsMap();
        object.put(name, value);
        return this;
    }

    public HashMap<String, JsonNode> getAsMap() {
        return type == NodeType.OBJECT ? (HashMap<String, JsonNode>) node : null;
    }

    public JsonNode get(int index) {
        if (type != NodeType.ARRAY)
            return NOT_EXIST;
        ArrayList<JsonNode> array = getAsArray();
        if (index < 0 || index >= array.size())
            return NOT_EXIST;
        return array.get(index);
    }

    public JsonNode add(int index, JsonNode element) {
        if (type != NodeType.ARRAY)
            return this;
        ArrayList<JsonNode> array = getAsArray();
        array.add(index, element);
        return this;
    }

    public JsonNode add(JsonNode element) {
        if (type != NodeType.ARRAY)
            return this;
        ArrayList<JsonNode> array = getAsArray();
        array.add(element);
        return this;
    }

    public ArrayList<JsonNode> getAsArray() {
        return type == NodeType.ARRAY ? (ArrayList<JsonNode>) node : null;
    }

    public String getAsString() {
        return type == NodeType.STRING ? (String) node : null;
    }

    public Integer getAsInt() {
        return type == NodeType.NUMBER ? Integer.parseInt((String) node) : null;
    }

    public Boolean getAsBoolean() {
        return type == NodeType.BOOLEAN ? (Boolean) node : null;
    }

    public String toJson() {
        switch (type) {
            case OBJECT: {
                Iterator<Map.Entry<String, JsonNode>> i = getAsMap().entrySet().iterator();
                if (!i.hasNext())
                    return "{}";
                StringBuilder sb = new StringBuilder();
                sb.append('{');
                for (;;) {
                    Map.Entry<String, JsonNode> e = i.next();
                    String key = e.getKey();
                    JsonNode value = e.getValue();
                    sb.append('"').append(key == node ? "this object" : key).append('"');
                    sb.append(':');
                    sb.append(value == node ? "this object" : value);
                    if (!i.hasNext())
                        return sb.append('}').toString();
                    sb.append(',').append(' ');
                }
            }
            case ARRAY: {
                Iterator<JsonNode> it = getAsArray().iterator();
                if (!it.hasNext())
                    return "[]";
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (;;) {
                    JsonNode e = it.next();
                    sb.append(e == node ? "this array" : e);
                    if (!it.hasNext())
                        return sb.append(']').toString();
                    sb.append(',').append(' ');
                }
            }
            case NUMBER: case BOOLEAN:
                return node.toString();
            case STRING: {
                char[] str = node.toString().toCharArray();
                StringBuilder sb = new StringBuilder();
                sb.append('"');
                for (char c : str) {
                    switch (c) {
                        case '\"': sb.append("\\\""); break;
                        case '\\': sb.append("\\\\"); break;
                        case '/': sb.append("\\/"); break;
                        case '\0': sb.append("\\0"); break;
                        case '\b': sb.append("\\b"); break;
                        case '\t': sb.append("\\t"); break;
                        case '\n': sb.append("\\n"); break;
                        case '\f': sb.append("\\f"); break;
                        case '\r': sb.append("\\r"); break;
                        default: sb.append(c);
                    }
                }
                sb.append('"');
                return sb.toString();
            }
            case NULL:
                return "null";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return toJson();
    }



    private static final JsonNode NOT_EXIST = new JsonNode(null, NodeType.NOT_EXIST);

    public static JsonNode newObject() {
        return new JsonNode(new HashMap<String, JsonNode>(), NodeType.OBJECT);
    }

    public static JsonNode newArray() {
        return new JsonNode(new ArrayList<JsonNode>(), NodeType.ARRAY);
    }

    public static JsonNode newNull() {
        return new JsonNode(null, NodeType.NULL);
    }

    public static JsonNode wrap(String string) {
        return new JsonNode(string, NodeType.STRING);
    }

    public static JsonNode wrap(Number num) {
        return new JsonNode(num, NodeType.NUMBER);
    }

    public static JsonNode wrap(boolean bool) {
        return new JsonNode(bool, NodeType.BOOLEAN);
    }

    private JsonNode(Object node, NodeType type) {
        this.node = node;
        this.type = type;
    }

    public static JsonNode parse(String json) {
        return parse(json.toCharArray(), new int[] {0});
    }

    private static JsonNode parse(char[] json, int[] p) {
        while (p[0] < json.length) {
            char c = json[p[0]];
            if (c == '-' || (c <= '9' && c >= '0'))
                return parseAsJsonNumber(json, p);
            switch (json[p[0]]) {
                case '{': return parseAsJsonObject(json, p);
                case '[': return parseAsJsonArray(json, p);
                case '"': return parseAsJsonString(json, p);
                case 't': p[0] += 4; return new JsonNode(true, NodeType.BOOLEAN);
                case 'f': p[0] += 5; return new JsonNode(false, NodeType.BOOLEAN);
                case 'n': return null;
                default: p[0]++;
            }
        }
        return null;
    }

    private static JsonNode parseAsJsonObject(char[] json, int[] p) {
        HashMap<String, JsonNode> map = new HashMap<String, JsonNode>();

        while (p[0] < json.length && json[p[0]] != '{')
            p[0]++;

        p[0]++;
        while (p[0] < json.length) {
            ignoreWhiteSpaces(json, p);
            if (json[p[0]] == '}')
                break;
            String key = parseAsString(json, p);
            JsonNode value = parse(json, p);
            map.put(key, value);
        }
        p[0]++;

        return new JsonNode(map, NodeType.OBJECT);
    }

    private static JsonNode parseAsJsonArray(char[] json, int[] p) {
        ArrayList<JsonNode> array = new ArrayList<JsonNode>();

        while (p[0] < json.length && json[p[0]] != '[')
            p[0]++;

        p[0]++;
        while (p[0] < json.length) {
            ignoreWhiteSpaces(json, p);
            if (json[p[0]] == ']')
                break;
            JsonNode value = parse(json, p);
            array.add(value);
        }
        p[0]++;

        return new JsonNode(array, NodeType.ARRAY);
    }

    private static JsonNode parseAsJsonNumber(char[] json, int[] p) {
        String num = parseAsNumber(json, p);
        return new JsonNode(num, NodeType.NUMBER);
    }

    private static String parseAsNumber(char[] json, int[] p) {
        StringBuilder sb = new StringBuilder();

        while (p[0] < json.length) {
            char c = json[p[0]];
            if ((c > '9' || c < '0') && c != '+' && c != '-' && c != '.' && c != 'e' && c != 'E')
                break;
            sb.append(c);
            p[0]++;
        }

        return sb.toString();
    }

    private static JsonNode parseAsJsonString(char[] json, int[] p) {
        String str = parseAsString(json, p);
        return new JsonNode(str, NodeType.STRING);
    }

    private static String parseAsString(char[] json, int[] p) {
        StringBuilder sb = new StringBuilder();

        while (p[0] < json.length && json[p[0]] != '"')
            p[0]++;

        p[0]++;
        while (p[0] < json.length) {
            if (json[p[0]] == '"')
                break;
            if (json[p[0]] == '\\') {
                p[0]++;
                switch (json[p[0]]) {
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u': sb.append(new Character((char) Integer.parseInt(new String(json, p[0]+1, 4), 16))); p[0] += 4; break;
                    default: sb.append(json[p[0]]);
                }
            } else {
                sb.append(json[p[0]]);
            }
            p[0]++;
        }
        p[0]++;

        return sb.toString();
    }

    private static void ignoreWhiteSpaces(char[] json, int[] p) {
        while (p[0] < json.length) {
            char c = json[p[0]];
            if (c != ' ' && c != '\n')
                break;
            p[0]++;
        }
    }

}
