package com.iecas.cmd.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML工具类
 */
@Slf4j
public class XmlUtils {
    
    /**
     * 预处理XML内容，处理属性中的特殊字符
     * 保留位运算符(>> 和 <<)的情况，其他特殊字符会被转义
     * 
     * @param xml 原始XML内容
     * @return 处理后的XML内容
     */
    public static String escapeXmlAttributes(String xml) {
        if (xml == null || xml.isEmpty()) {
            return xml;
        }
        
        // 首先修复XML头中的引号问题
        Pattern xmlDeclPattern = Pattern.compile("(<\\?xml[^>]*?)(\")(.*?)(\")(.*?\\?>)");
        Matcher xmlDeclMatcher = xmlDeclPattern.matcher(xml);
        String fixedXmlDecl = xml;
        
        if (xmlDeclMatcher.find()) {
            fixedXmlDecl = xmlDeclMatcher.replaceFirst("$1$2$3$4$5");
        }
        
        // 处理已经双重转义的字符
        String processedXml = fixedXmlDecl
                .replace("&amp;amp;lt;", "&lt;")
                .replace("&amp;amp;gt;", "&gt;")
                .replace("&amp;amp;amp;", "&amp;")
                .replace("&amp;lt;", "&lt;")
                .replace("&amp;gt;", "&gt;")
                .replace("&quot;", "\""); // 先直接处理属性值中的&quot;
        
        // 修复属性引号问题 - 先找到所有属性名=&quot;的情况
        // 注意: 这是个特殊的模式，修复某些错误转义场景
        Pattern attrPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)=&quot;([^\"&]*)&quot;");
        Matcher attrMatcher = attrPattern.matcher(processedXml);
        StringBuffer sbAttr = new StringBuffer();
        
        while (attrMatcher.find()) {
            String attrName = attrMatcher.group(1);
            String attrValue = attrMatcher.group(2);
            attrMatcher.appendReplacement(sbAttr, attrName + "=\"" + attrValue + "\"");
        }
        attrMatcher.appendTail(sbAttr);
        processedXml = sbAttr.toString();
        
        // 处理一般特殊字符
        // 使用正则表达式匹配属性值中的特殊字符
        // 匹配属性中的特殊字符模式，增强版本支持更多特殊字符
        Pattern pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)=\"([^\"]*?)([<>&'\"])([^\"]*)\"");
        Matcher matcher = pattern.matcher(processedXml);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String attrName = matcher.group(1);
            String before = matcher.group(2);
            String specialChar = matcher.group(3);
            String after = matcher.group(4);
            
            // 转义特殊字符，但排除特定情况
            String escapedChar = specialChar;
            
            // 检查是否是位运算符 (>> 或 <<)
            boolean isShiftOperator = (specialChar.equals("<") && before.endsWith("<")) || 
                                     (specialChar.equals(">") && after.startsWith(">"));
            
            // 检查是否是表达式中的操作符
            boolean isOperator = (specialChar.equals("<") || specialChar.equals(">")) && 
                                (before.endsWith(" ") || after.startsWith(" ") || 
                                 before.endsWith("(") || after.startsWith(")") ||
                                 before.endsWith("=") || after.startsWith("="));
            
            // 检查引号是否出现在合法上下文中
            boolean isLegalQuote = specialChar.equals("\"") && 
                                   (before.endsWith("=") || after.startsWith("=") || 
                                    before.isEmpty() || after.isEmpty());
            
            // 只在不是特殊情况下转义
            if (!isShiftOperator && !isOperator && !isLegalQuote) {
                escapedChar = escapeXmlChar(specialChar);
            }
            
            matcher.appendReplacement(sb, attrName + "=\"" + before + escapedChar + after + "\"");
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 转义单个XML特殊字符
     * 
     * @param c 特殊字符
     * @return 转义后的字符串
     */
    private static String escapeXmlChar(String c) {
        switch (c) {
            case "<": return "&lt;";
            case ">": return "&gt;";
            case "&": return "&amp;";
            case "'": return "&apos;";
            case "\"": return "&quot;";
            default: return c;
        }
    }
    
    /**
     * 检查XML中是否存在未转义的特殊字符
     * 
     * @param xml XML内容
     * @return 包含未转义特殊字符的行号和内容
     */
    public static String checkUnescapedChars(String xml) {
        StringBuilder result = new StringBuilder();
        boolean hasUnescapedChars = false;
        
        String[] lines = xml.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 排除CDATA部分和位运算符的情况
            if (!line.contains("<![CDATA[") && !line.contains("]]>")) {
                if ((line.contains("<") && !line.contains("<<") && !line.contains("</") && !line.contains("<node") && !line.contains("<enumRange")) || 
                    (line.contains(">") && !line.contains(">>") && !line.contains("/>") && !line.contains(">?")) || 
                    (line.contains("&") && !line.contains("&lt;") && !line.contains("&gt;") && !line.contains("&amp;") && !line.contains("&apos;") && !line.contains("&quot;"))) {
                    hasUnescapedChars = true;
                    result.append("[XML解析] 第").append(i + 1).append("行: ").append(line).append("\n");
                }
            }
        }
        
        if (hasUnescapedChars) {
            result.append("[XML解析] 警告: 发现未转义的特殊字符，但排除了位运算符和XML标签");
        }
        
        return hasUnescapedChars ? result.toString() : "";
    }
    
    /**
     * 一键处理XML文件，自动处理特殊字符并进行检查
     * 
     * @param xml 原始XML内容
     * @return 处理后的XML内容
     */
    public static String processXml(String xml) {
        // 1. 处理特殊字符
        String processedXml = escapeXmlAttributes(xml);
        
        // 修复XML头中可能存在的问题
        processedXml = fixXmlDeclaration(processedXml);
        
        // 2. 检查是否还有未处理的特殊字符
        String checkResult = checkUnescapedChars(processedXml);
        if (!checkResult.isEmpty()) {
            log.debug(checkResult);
        }
        
        return processedXml;
    }
    
    /**
     * 修复XML声明中的问题
     * 
     * @param xml XML内容
     * @return 修复后的XML内容
     */
    private static String fixXmlDeclaration(String xml) {
        if (xml == null || !xml.trim().startsWith("<?xml")) {
            return xml;
        }
        
        // 提取并修复XML声明
        int endPos = xml.indexOf("?>");
        if (endPos > 0) {
            String declaration = xml.substring(0, endPos + 2);
            String content = xml.substring(endPos + 2);
            
            // 修复各种双引号问题
            declaration = declaration
                .replaceAll("&quot;", "\"")
                .replaceAll("version=\"1.0&quot;", "version=\"1.0\"")
                .replaceAll("version=\"1.0\\\"", "version=\"1.0\"")
                .replaceAll("encoding=\"([^\"]+)&quot;", "encoding=\"$1\"")
                .replaceAll("encoding=\"([^\"]+)\\\"", "encoding=\"$1\"");
            
            // 确保XML声明格式正确
            if (!declaration.contains("version=\"1.0\"")) {
                declaration = declaration.replace("<?xml", "<?xml version=\"1.0\"");
            }
            if (!declaration.contains("encoding=")) {
                declaration = declaration.replace("?>", " encoding=\"UTF-8\"?>");
            }
            
            return declaration + content;
        }
        
        return xml;
    }
    
    /**
     * 将XML属性值转为CDATA包装形式
     * 
     * @param xml 原始XML内容
     * @return 处理后的XML内容
     */
    public static String wrapExpressionWithCDATA(String xml) {
        // 匹配fwdExpr和bwdExpr属性
        Pattern pattern = Pattern.compile("(fwdExpr|bwdExpr)=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(xml);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String attrName = matcher.group(1);
            String expression = matcher.group(2);
            
            // 反转义表达式
            String unescapedExpr = unescapeXml(expression);
            
            // 用CDATA包装处理后的表达式
            String replacement = attrName + "=\"<![CDATA[" + unescapedExpr + "]]>\"";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 反转义XML特殊字符
     * 
     * @param text 转义后的文本
     * @return 原始文本
     */
    public static String unescapeXml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return text.replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&amp;", "&")
                  .replace("&apos;", "'")
                  .replace("&quot;", "\"");
    }
    
    /**
     * 独立测试处理protocol.xml文件
     */
    public static void main(String[] args) {
        try {
            log.debug("===== XML处理工具测试 =====");
            
            // 指定protocol.xml文件路径
            String xmlPath = "protocol.xml";
            if (args.length > 0) {
                xmlPath = args[0];
            }
            
            // 读取XML文件
            String xml = readFileContent(xmlPath);
            log.debug("原始XML长度: " + xml.length());
            
            // 使用新方法修复XML
            String fixedXml = repairXmlForJaxb(xml);
            
            // 将修复后的内容写入新文件
            String outputPath = "fixed_" + xmlPath;
            writeFileContent(outputPath, fixedXml);
            log.debug("已将修复后的XML写入文件: " + outputPath);
            
            // 对比检查
            log.debug("=== 修复前后对比 ===");
            log.debug("* 修复前的XML文件大小: " + xml.length() + " 字节");
            log.debug("* 修复后的XML文件大小: " + fixedXml.length() + " 字节");
            int changes = 0;
            for (int i = 0; i < Math.min(xml.length(), fixedXml.length()); i++) {
                if (xml.charAt(i) != fixedXml.charAt(i)) {
                    changes++;
                }
            }
            log.debug("* 字符差异数量: " + changes + " 字符");
            log.debug("* 差异比例: " + String.format("%.2f%%", (double)changes / xml.length() * 100));
            
        } catch (Exception e) {
            log.error("处理XML时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 读取文件内容
     */
    private static String readFileContent(String filePath) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        byte[] bytes = java.nio.file.Files.readAllBytes(path);
        
        // 移除BOM标记（如果存在）
        if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            bytes = java.util.Arrays.copyOfRange(bytes, 3, bytes.length);
        }
        
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * 写入文件内容
     */
    private static void writeFileContent(String filePath, String content) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        java.nio.file.Files.write(path, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    /**
     * 主要的XML修复方法
     * 此方法尝试多种修复手段，确保XML可以被正确解析
     */
    public static String repairXmlForJaxb(String xml) {
        try {
            log.debug("[XML修复] 开始修复XML");
            
            // 1. 添加XML声明（如果没有）
            if (!xml.trim().startsWith("<?xml")) {
                xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
                log.debug("[XML修复] 添加XML声明");
            }
            
            // 2. 转义属性中的特殊字符
            xml = escapeXmlAttributes(xml);
            log.debug("[XML修复] 转义属性中的特殊字符");
            
            // 3. 处理表达式
            xml = wrapExpressionWithCDATA(xml);
            log.debug("[XML修复] 包装表达式为CDATA");
            
            // 4. 修复其他JAXB解析问题
            xml = fixJaxbParsingIssues(xml);
            log.debug("[XML修复] 修复JAXB解析问题");
            
            log.debug("[XML修复] XML修复完成");
            return xml;
            
        } catch (Exception e) {
            log.error("[XML修复] 修复XML过程中发生错误: {}", e.getMessage(), e);
            return xml; // 返回原始XML
        }
    }
    
    /**
     * 专用方法处理XML中的双引号问题
     * 检测和修复XML格式问题，使其可以被正确解析
     * 这个方法特别针对JAXB解析错误
     * 
     * @param xmlContent XML内容字符串
     * @return 修复后的XML字符串
     */
    public static String fixJaxbParsingIssues(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return xmlContent;
        }
        
        String result = xmlContent;
        
        try {
            // 1. 处理XML声明头（重点处理双引号）
            int xmlDeclEnd = result.indexOf("?>");
            if (xmlDeclEnd > 0) {
                String xmlDecl = result.substring(0, xmlDeclEnd);
                
                // 修复双引号问题（使用简单替换避免复杂正则）
                if (xmlDecl.contains("&quot;")) {
                    xmlDecl = xmlDecl.replace("&quot;", "\"");
                }
                
                // 修复encoding属性中的引号问题
                if (xmlDecl.contains("encoding=\"UTF-8&quot;")) {
                    xmlDecl = xmlDecl.replace("encoding=\"UTF-8&quot;", "encoding=\"UTF-8\"");
                }
                if (xmlDecl.contains("version=\"1.0&quot;")) {
                    xmlDecl = xmlDecl.replace("version=\"1.0&quot;", "version=\"1.0\"");
                }
                
                // 重新组装
                result = xmlDecl + "?>" + result.substring(xmlDeclEnd + 2);
                
                log.debug("[XML修复] 处理后的XML声明: " + xmlDecl + "?>");
            }
            
            // 2. 修复属性中的引号问题
            // 首先修复属性值中的&quot;
            result = result.replace("&quot;", "\"");
            
            // 3. 修复多余的引号问题（处理类似：name="value"value这种情况)
            result = result.replaceAll("=\"([^\"]*)\"([a-zA-Z0-9_]+)", "=\"$1\" $2");
            
            // 4. 修复自闭合标签
            result = ensureTagsClosed(result);
            
            return result;
        } catch (Exception e) {
            log.error("[XML修复] 修复XML过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            // 返回原始内容，确保不会丢失数据
            return xmlContent;
        }
    }
    
    /**
     * 检查XML是否存在问题
     */
    private static boolean hasNoXmlProblems(String xml) {
        // 检查一些明显的问题
        boolean hasQuoteProblems = xml.contains("&quot;");
        boolean hasLtProblems = xml.contains("&lt;") && !xml.contains("<![CDATA[");
        boolean hasGtProblems = xml.contains("&gt;") && !xml.contains("<![CDATA[");
        
        return !hasQuoteProblems && !hasLtProblems && !hasGtProblems;
    }
    
    /**
     * 规范化引号包裹的属性值
     */
    private static String normalizeQuotedAttributes(String xml) {
        // 修复属性值的引号问题
        String result = xml;
        
        // 处理引号之间的多余空格
        result = result.replaceAll("\\s*=\\s*\"", "=\"");
        
        // 处理可能存在的双引号问题
        result = result.replaceAll("\"\"", "\"");
        
        return result;
    }
    
    /**
     * 确保XML标签正确关闭
     */
    private static String ensureTagsClosed(String xml) {
        // 这是一个简化版实现，仅处理最常见的自闭合标签问题
        return xml.replace("/>", " />");
    }
} 