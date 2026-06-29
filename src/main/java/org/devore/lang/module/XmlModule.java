package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.DSecurity;
import org.devore.lang.Env;
import org.devore.lang.token.DBool;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.lang.token.DTable;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML处理
 */
public class XmlModule extends DModule {
    private static final DString TYPE = DString.valueOf("type");
    private static final DString NAME = DString.valueOf("name");
    private static final DString ATTRS = DString.valueOf("attrs");
    private static final DString CHILDREN = DString.valueOf("children");
    private static final DString TEXT = DString.valueOf("text");
    private static final DString TARGET = DString.valueOf("target");
    private static final DString DATA = DString.valueOf("data");

    /**
     * 创建Xml模块实例
     */
    public XmlModule() {
        super("xml");
    }

    /**
     * 初始化Xml模块，注册对外过程
     */
    @Override
    public void init(Env dEnv) {
        initXmlProcedures(dEnv); // XML读写
    }

    /**
     * 注册XML读写、构造和合法性判断过程
     */
    private void initXmlProcedures(Env dEnv) {
        dEnv.addTokenProcedure("xml-read-string", (args, env) ->
                readString(stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-write-string", (args, env) ->
                DString.valueOf(writeString(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-read-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            return readFile(stringArg(args.get(0)), StandardCharsets.UTF_8);
        }, 1, false);
        dEnv.addTokenProcedure("xml-read-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            return readFile(stringArg(args.get(0)), charsetArg(args.get(1)));
        }, 2, false);
        dEnv.addTokenProcedure("xml-write-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            writeFile(stringArg(args.get(0)), args.get(1), StandardCharsets.UTF_8);
            return DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("xml-write-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            writeFile(stringArg(args.get(0)), args.get(1), charsetArg(args.get(2)));
            return DWord.NIL;
        }, 3, false);
        dEnv.addTokenProcedure("xml-document", (args, env) ->
                documentNode(listArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-element", (args, env) ->
                elementNode(stringArg(args.get(0)), tableArg(args.get(1)), listArg(args.get(2))), 3, false);
        dEnv.addTokenProcedure("xml-text", (args, env) ->
                textNode("text", stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-cdata", (args, env) ->
                textNode("cdata", stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-comment", (args, env) ->
                textNode("comment", stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("xml-pi", (args, env) ->
                piNode(stringArg(args.get(0)), stringArg(args.get(1))), 2, false);
        dEnv.addTokenProcedure("xml?", (args, env) ->
                DBool.valueOf(isXml(args.get(0))), 1, false);
    }

    /**
     * 读取XML字符串并转换为节点表
     */
    private static DTable readString(String content) {
        try {
            DocumentBuilderFactory factory = documentBuilderFactory();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                /**
                 * 将XML警告作为解析异常抛出
                 */
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                /**
                 * 将XML错误作为解析异常抛出
                 */
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                /**
                 * 将XML致命错误作为解析异常抛出
                 */
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            Document document = builder.parse(new InputSource(new StringReader(content)));
            return documentNode(children(document.getChildNodes()));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DevoreRuntimeException("解析XML字符串失败: " + e.getMessage());
        }
    }

    /**
     * 按指定字符集读取XML文件
     */
    private static DTable readFile(String file, Charset charset) {
        Path path = Paths.get(file);
        try {
            return readString(new String(Files.readAllBytes(path), charset));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取XML文件失败: " + path + ", " + e.getMessage());
        }
    }

    /**
     * 将XML节点表写为字符串
     */
    private static String writeString(DToken token) {
        return writeNode(tableArg(token));
    }

    /**
     * 按指定字符集写入XML文件
     */
    private static void writeFile(String file, DToken token, Charset charset) {
        Path path = Paths.get(file);
        try {
            Files.write(path, writeString(token).getBytes(charset));
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入XML文件失败: " + path + ", " + e.getMessage());
        }
    }

    /**
     * 创建安全配置的XML文档构建工厂
     */
    private static DocumentBuilderFactory documentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setCoalescing(false);
        factory.setIgnoringComments(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    /**
     * 将DOM子节点列表转换为Devore列表
     */
    private static DList children(NodeList nodeList) {
        List<DToken> children = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            DToken child = toToken(nodeList.item(i));
            if (child != DWord.NIL)
                children.add(child);
        }
        return DList.valueOf(children);
    }

    /**
     * 将DOM节点转换为Devore节点表
     */
    private static DToken toToken(Node node) {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                return documentNode(children(node.getChildNodes()));
            case Node.ELEMENT_NODE:
                return elementNode(node.getNodeName(), attrs(node.getAttributes()), children(node.getChildNodes()));
            case Node.TEXT_NODE:
                return textNode("text", node.getNodeValue());
            case Node.CDATA_SECTION_NODE:
                return textNode("cdata", node.getNodeValue());
            case Node.COMMENT_NODE:
                return textNode("comment", node.getNodeValue());
            case Node.PROCESSING_INSTRUCTION_NODE:
                return piNode(node.getNodeName(), node.getNodeValue());
            default:
                return DWord.NIL;
        }
    }

    /**
     * 将DOM属性集合转换为Devore表
     */
    private static DTable attrs(NamedNodeMap nodeMap) {
        Map<DToken, DToken> attrs = new HashMap<>();
        for (int i = 0; i < nodeMap.getLength(); ++i) {
            Node attr = nodeMap.item(i);
            attrs.put(DString.valueOf(attr.getNodeName()), DString.valueOf(attr.getNodeValue()));
        }
        return DTable.valueOf(attrs);
    }

    /**
     * 构造XML文档节点表
     */
    private static DTable documentNode(DList children) {
        Map<DToken, DToken> node = baseNode("document");
        node.put(CHILDREN, children);
        return DTable.valueOf(node);
    }

    /**
     * 构造并校验XML元素节点表
     */
    private static DTable elementNode(String name, DTable attrs, DList children) {
        validateName(name, "XML元素名");
        validateAttrs(attrs);
        Map<DToken, DToken> node = baseNode("element");
        node.put(NAME, DString.valueOf(name));
        node.put(ATTRS, attrs);
        node.put(CHILDREN, children);
        return DTable.valueOf(node);
    }

    /**
     * 构造XML文本类节点表
     */
    private static DTable textNode(String type, String text) {
        Map<DToken, DToken> node = baseNode(type);
        node.put(TEXT, DString.valueOf(text));
        return DTable.valueOf(node);
    }

    /**
     * 构造并校验XML处理指令节点表
     */
    private static DTable piNode(String target, String data) {
        validateName(target, "XML处理指令目标");
        if ("xml".equalsIgnoreCase(target))
            throw new DevoreRuntimeException("XML处理指令目标不能是xml.");
        if (data.contains("?>"))
            throw new DevoreRuntimeException("XML处理指令内容不能包含?>.");
        Map<DToken, DToken> node = baseNode("pi");
        node.put(TARGET, DString.valueOf(target));
        node.put(DATA, DString.valueOf(data));
        return DTable.valueOf(node);
    }

    private static Map<DToken, DToken> baseNode(String type) {
        Map<DToken, DToken> node = new HashMap<>();
        node.put(TYPE, DString.valueOf(type));
        return node;
    }

    /**
     * 将XML节点表序列化为字符串片段
     */
    private static String writeNode(DTable node) {
        String type = requiredString(node, TYPE);
        switch (type) {
            case "document":
                return writeChildren(requiredList(node), true);
            case "element":
                return writeElement(node);
            case "text":
                return escapeText(requiredString(node, TEXT));
            case "cdata":
                return writeCdata(requiredString(node, TEXT));
            case "comment":
                return writeComment(requiredString(node, TEXT));
            case "pi":
                return writePi(node);
            default:
                throw new DevoreRuntimeException("未知XML节点类型: " + type);
        }
    }

    /**
     * 序列化XML元素节点
     */
    private static String writeElement(DTable node) {
        String name = requiredString(node, NAME);
        DTable attrs = requiredTable(node);
        DList children = requiredList(node);
        validateName(name, "XML元素名");
        StringBuilder builder = new StringBuilder("<").append(name);
        for (DToken key : attrs.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = attrs.get(key);
            if (!(value instanceof DString))
                throw new DevoreCastException(value.type(), "string");
            validateName(key.toString(), "XML属性名");
            builder.append(' ')
                    .append(key)
                    .append("=\"")
                    .append(escapeAttr(value.toString()))
                    .append('"');
        }
        if (children.size() == 0)
            return builder.append("/>").toString();
        return builder.append('>')
                .append(writeChildren(children, false))
                .append("</")
                .append(name)
                .append('>')
                .toString();
    }

    /**
     * 序列化XML子节点列表
     */
    private static String writeChildren(DList children, boolean document) {
        StringBuilder builder = new StringBuilder();
        int rootCount = 0;
        for (DToken child : children.toList()) {
            DTable node = tableArg(child);
            if ("element".equals(requiredString(node, TYPE)))
                ++rootCount;
            builder.append(writeNode(node));
        }
        if (document && rootCount != 1)
            throw new DevoreRuntimeException("XML文档必须包含且只能包含一个根元素.");
        return builder.toString();
    }

    /**
     * 序列化CDATA节点内容
     */
    private static String writeCdata(String text) {
        if (text.contains("]]>"))
            throw new DevoreRuntimeException("XML CDATA内容不能包含]]>.");
        return "<![CDATA[" + text + "]]>";
    }

    /**
     * 序列化XML注释内容
     */
    private static String writeComment(String text) {
        if (text.contains("--") || text.endsWith("-"))
            throw new DevoreRuntimeException("XML注释内容不能包含--，也不能以-结尾.");
        return "<!--" + text + "-->";
    }

    /**
     * 序列化XML处理指令
     */
    private static String writePi(DTable node) {
        String target = requiredString(node, TARGET);
        String data = requiredString(node, DATA);
        validateName(target, "XML处理指令目标");
        if ("xml".equalsIgnoreCase(target))
            throw new DevoreRuntimeException("XML处理指令目标不能是xml.");
        if (data.contains("?>"))
            throw new DevoreRuntimeException("XML处理指令内容不能包含?>.");
        return data.isEmpty() ? "<?" + target + "?>" : "<?" + target + " " + data + "?>";
    }

    /**
     * 转义XML文本内容
     */
    private static String escapeText(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 转义XML属性值
     */
    private static String escapeAttr(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\t':
                    builder.append("&#x9;");
                    break;
                case '\n':
                    builder.append("&#xA;");
                    break;
                case '\r':
                    builder.append("&#xD;");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 判断Devore值是否为合法XML节点
     */
    private static boolean isXml(DToken token) {
        if (!(token instanceof DTable))
            return false;
        try {
            validateNode((DTable) token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * 校验XML节点表结构
     */
    private static void validateNode(DTable node) {
        String type = requiredString(node, TYPE);
        switch (type) {
            case "document":
                writeChildren(requiredList(node), true);
                break;
            case "element":
                writeElement(node);
                break;
            case "text":
                requiredString(node, TEXT);
                break;
            case "cdata":
                writeCdata(requiredString(node, TEXT));
                break;
            case "comment":
                writeComment(requiredString(node, TEXT));
                break;
            case "pi":
                writePi(node);
                break;
            default:
                throw new DevoreRuntimeException("未知XML节点类型: " + type);
        }
    }

    /**
     * 校验XML属性表
     */
    private static void validateAttrs(DTable attrs) {
        for (DToken key : attrs.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = attrs.get(key);
            if (!(value instanceof DString))
                throw new DevoreCastException(value.type(), "string");
            validateName(key.toString(), "XML属性名");
        }
    }

    /**
     * 校验XML名称格式
     */
    private static void validateName(String name, String label) {
        if (name.isEmpty())
            throw new DevoreRuntimeException(label + "不能为空.");
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c) || c == '<' || c == '>' || c == '/' || c == '=' || c == '"' || c == '\'')
                throw new DevoreRuntimeException(label + "包含非法字符: " + name);
        }
    }

    /**
     * 读取节点表中的必需字符串字段
     */
    private static String requiredString(DTable table, DString key) {
        DToken value = table.get(key);
        if (!(value instanceof DString))
            throw new DevoreCastException(value.type(), "string");
        return value.toString();
    }

    /**
     * 读取元素节点中的属性表
     */
    private static DTable requiredTable(DTable table) {
        DToken value = table.get(XmlModule.ATTRS);
        if (!(value instanceof DTable))
            throw new DevoreCastException(value.type(), "table");
        return (DTable) value;
    }

    /**
     * 读取节点表中的子节点列表
     */
    private static DList requiredList(DTable table) {
        DToken value = table.get(XmlModule.CHILDREN);
        if (!(value instanceof DList))
            throw new DevoreCastException(value.type(), "list");
        return (DList) value;
    }

    /**
     * 校验并取得表参数
     */
    private static DTable tableArg(DToken token) {
        if (!(token instanceof DTable))
            throw new DevoreCastException(token.type(), "table");
        return (DTable) token;
    }

    /**
     * 校验并取得列表参数
     */
    private static DList listArg(DToken token) {
        if (!(token instanceof DList))
            throw new DevoreCastException(token.type(), "list");
        return (DList) token;
    }

    /**
     * 校验并取得字符串参数
     */
    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    /**
     * 校验并取得字符集参数
     */
    private static Charset charsetArg(DToken token) {
        try {
            return Charset.forName(stringArg(token));
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException("字符集不存在: " + token);
        }
    }
}
