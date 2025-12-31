package com.iecas.cmd.parser;

import com.alibaba.fastjson.JSONObject;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.util.XmlUtils;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 协议解析器
 * 负责解析 XML 格式的协议定义文件，并构建协议模型
 */
@Slf4j
public class ProtocolParser {

    public static void main(String[] args) throws IOException {
        ProtocolParser parser = new ProtocolParser();
        // 读取 protocol.xml 文件内容
        String protocolXml = parser.readFileWithoutBOM("/Users/lidong/project/96v2-cmd/cmd-codec/protocol.xml");

        // 执行解析
        Protocol protocol = parser.parse(protocolXml);

        log.debug(JSONObject.toJSON(protocol).toString());
    }

    /**
     * 解析协议XML字符串
     * @param xml XML字符串
     * @return 协议对象
     * @throws ProtocolParseException 解析异常
     */
    public Protocol parse(String xml) throws ProtocolParseException {
        try {
            JAXBContext context = JAXBContext.newInstance(Protocol.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            StringReader reader = new StringReader(xml);
            return (Protocol) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new ProtocolParseException("解析协议XML失败", e);
        }
    }

    /**
     * 解析协议XML字符串（使用JAXB）
     * @param xml XML字符串
     * @return 协议对象
     * @throws ProtocolParseException 解析异常
     */
    public Protocol parseWithJAXB(String xml) throws ProtocolParseException {
        try {
            log.debug("[协议解析] 原始XML长度: {}", xml.length());
            
            // 修复XML声明
            if (!xml.trim().startsWith("<?xml")) {
                xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
                log.debug("[协议解析] 已修复的XML声明: {}", xml.substring(0, xml.indexOf("?>") + 2));
            }
            
            // 使用XmlUtils进行预处理
            String processedXml = XmlUtils.repairXmlForJaxb(xml);
            
            JAXBContext context = JAXBContext.newInstance(Protocol.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            StringReader reader = new StringReader(processedXml);
            Protocol protocol = (Protocol) unmarshaller.unmarshal(reader);
            
            log.debug("[协议解析] 解析成功");
            return protocol;
            
        } catch (JAXBException e) {
            log.error("[协议解析] JAXB解析异常: {}", e.getMessage());
            if (e.getLinkedException() != null) {
                log.error("[协议解析] 关联异常: {}", e.getLinkedException().getMessage());
            }
            throw new ProtocolParseException("JAXB解析协议XML失败: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("[协议解析] 其他异常: {}", e.getMessage());
            throw new ProtocolParseException("解析协议XML失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从文件读取内容，自动处理BOM
     * @param filePath 文件路径
     * @return 文件内容
     * @throws IOException IO异常
     */
    public String readFileWithoutBOM(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        
        // 检查并移除UTF-8 BOM
        if (bytes.length >= 3 && 
            bytes[0] == (byte) 0xEF && 
            bytes[1] == (byte) 0xBB && 
            bytes[2] == (byte) 0xBF) {
            // 移除BOM
            byte[] withoutBOM = new byte[bytes.length - 3];
            System.arraycopy(bytes, 3, withoutBOM, 0, withoutBOM.length);
            return new String(withoutBOM, StandardCharsets.UTF_8);
        }
        
        return new String(bytes, StandardCharsets.UTF_8);
    }
} 