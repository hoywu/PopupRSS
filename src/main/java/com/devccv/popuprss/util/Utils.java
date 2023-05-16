package com.devccv.popuprss.util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static Proxy getProxyInstance() {
        Proxy proxy;
        if ("HTTP".equals(ConfigManager.CONFIG.getProxy())) {
            String proxyURL = ConfigManager.CONFIG.getProxyURL().split("//")[1];
            String[] split = proxyURL.split(":");
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1].replace("/", ""))));
        } else if ("SOCKS".equals(ConfigManager.CONFIG.getProxy())) {
            String proxyURL = ConfigManager.CONFIG.getProxyURL().split("//")[1];
            String[] split = proxyURL.split(":");
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(split[0], Integer.parseInt(split[1].replace("/", ""))));
        } else {
            proxy = Proxy.NO_PROXY;
        }
        return proxy;
    }

    public static URL getURLInstance() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, URISyntaxException, MalformedURLException {
        return new URI(Encrypt.decryptWithUserName(ConfigManager.CONFIG.getRssLink())).toURL();
    }
}
