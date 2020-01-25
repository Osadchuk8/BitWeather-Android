package com.example.bitweather;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectivityHelper {

/**
 * Checks if internet is reachable.
 * Async, must be called from background thread
 */
    public static boolean isOnlineBackground() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) {
            Log.d("``connectivity:", e.getLocalizedMessage());
            return false;
        }
    }


    public static void isOnline(CompletionString completion){

        if (isOnlineBackground()) {
            completion.completionStringOk("ConnectivityHelper: is online");
        }else{
            completion.completionStringError("Looks like there is no connection to the internet, please check the settings.");
        }

    }


}
