package com.example.customerdisplayhandler.core.network;

import android.util.Log;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.helpers.ISocketMessageProcessHelper;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TcpMessageSenderImpl implements ITcpMessageSender {
    private static final String TAG = TcpMessageSenderImpl.class.getSimpleName();
    private final ITcpMessageListener tcpMessageListener;
    private final ISocketMessageProcessHelper socketMessageProcessHelper;

    public TcpMessageSenderImpl(ITcpMessageListener tcpMessageListener, ISocketMessageProcessHelper socketMessageProcessHelper) {
        this.tcpMessageListener = tcpMessageListener;
        this.socketMessageProcessHelper = socketMessageProcessHelper;
    }

    @Override
    public Completable sendMessageAndCatchAcknowledgement(String serverId, Socket socket, String message, String messageId, String clientId) {
        return sendMessageToServer(serverId, socket, message)
                .andThen(
                        tcpMessageListener.getServerMessageSubject()
                                .filter(serverMessage -> {
                                    String acknowledgementMessageId = socketMessageProcessHelper.getAcknowledgeMessageId(serverMessage.second, clientId);
                                    return acknowledgementMessageId != null && acknowledgementMessageId.equals(messageId);
                                })
                                .firstOrError()
                                .timeout(NetworkConstants.WAITING_FOR_ACKNOWLEDGEMENT_TIMEOUT, TimeUnit.MILLISECONDS) // Wait until the timeout expires
                                .flatMapCompletable(serverMessage -> Completable.complete())
                                .doOnError(error -> Log.e(TAG, "Acknowledgement not received from the customer display within the timeout period"))
                );
    }

    public Completable sendOneWayMessage(Socket socket,String serverId, String message){
        return sendMessageToServer(serverId, socket, message);
    }


    private Completable sendMessageToServer(String serverId, Socket socket, String message) {
        return Completable.create((emitter) -> {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                Log.i(TAG, "Message successfully left this end, receiver: " + serverId + ", message: " + message);
                emitter.onComplete();
            } catch (EOFException eof) {
                Log.w(TAG, "Customer display disconnected, cannot send message, " + "customer display id: " + serverId);
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception("Customer display disconnected, cannot send message."));
                }
            } catch (IOException e) {
                Log.w(TAG, "Error occurred while sending message to customer display, " + "customer display id: " + serverId, e);
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception("Error occurred while sending message to customer display"));
                }
            }
        }).subscribeOn(Schedulers.io()); // Perform operation on IO thread
    }
}
