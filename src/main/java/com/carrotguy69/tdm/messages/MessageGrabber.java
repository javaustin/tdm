package com.carrotguy69.tdm.messages;

import java.util.Map;

import static com.carrotguy69.cxyz.messages.MessageUtils.formatPlaceholders;
import static com.carrotguy69.tdm.TDM.messagesYML;

public class MessageGrabber {
    public static String grab(String key, Map<String, Object> values) {
        String template = messagesYML.getString(key, key);

        return formatPlaceholders(template, values);
    }

    public static String grab(TDMMessageKey key, Map<String, Object> values) {
        return grab(key.getPath(), values);
    }

    public static String grab(TDMMessageKey key) {
        return grab(key.getPath(), Map.of());
    }

    public static String grab(String key) {
        return grab(key, Map.of());
    }
}
