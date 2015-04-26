package ua.atamurius.vk.music;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18n {

    private final ResourceBundle bundle;

    public I18n(Class<?> ctx) {
        bundle = ResourceBundle.getBundle(ctx.getName());
    }

    public String l(String key, Object ... params) {
        String msg = bundle.getString(key);
    	if (params.length > 0) {
    		msg = MessageFormat.format(msg, params);
    	}
    	return msg;
    }
}
