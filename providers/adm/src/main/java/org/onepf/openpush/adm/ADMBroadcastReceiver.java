package org.onepf.openpush.adm;

import com.amazon.device.messaging.ADMMessageReceiver;

/**
 * Created by krozov on 06.09.14.
 */
public class ADMBroadcastReceiver extends ADMMessageReceiver {
    public ADMBroadcastReceiver() {
        super(ADMService.class);
    }
}
