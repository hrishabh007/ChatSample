package com.app.realtime.chatsample.xmpp;

import com.app.realtime.chatsample.model.ChatItem;
import com.app.realtime.chatsample.model.ChatStateModel;
import com.app.realtime.chatsample.model.PresenceModel;



public class XmppCustomEventListener implements XMPPEventReceiver.XmppCustomEventListenerBase {

    public void onNewMessageReceived(ChatItem chatItem) {}
    public void onLoggedIn() {}
    public void onSignupSuccess() {}
    public void onSignupFailed(String error) {}
    public void onAuthenticated() {}
    public void onReConnectionError() {}
    public void onConnected() {}
    public void onReConnected() {}
    public void onConnectionClosed() {}
    public void onReConnection() {}
    public void onLoginFailed() {}
    public void onPresenceChanged(PresenceModel presenceModel) {}
    public void onChatStateChanged(ChatStateModel chatStateModel){}
    public void onSubscriptionRequest(String fromuser){}
}
