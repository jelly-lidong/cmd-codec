package com.iecas.cmd.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class AddressUtil {

    private static String localHost;

    /**
     * 获取有网卡的地址
     *
     * @return
     */
    public static String getNetCardLocalHost() {
        if (localHost != null) {
            return localHost;
        }
        List<String> list = new ArrayList<>();
        try {
            list.addAll(getLocalIPs());
        } catch (SocketException e) {
            log.error(e.getMessage());
        }
        if (list.size() == 0) {
            return localHost = getLocalHost();
        }

        return localHost = list.get(0);
    }

    public static String getLocalHost() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
//            log.error("UnknownHostException : " + e.getMessage());
            //ignore
        }
        assert localHost != null;
        return localHost.getHostAddress();
    }

    public static List<String> getLocalIPs() throws SocketException {
        List<String> list = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback() || networkInterface.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isAnyLocalAddress()) {
                    continue;
                }
                list.add(inetAddress.getHostAddress());
            }
        }
        return list;
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName("5.20.202.16"));
        System.out.println(ni);
    }
}
