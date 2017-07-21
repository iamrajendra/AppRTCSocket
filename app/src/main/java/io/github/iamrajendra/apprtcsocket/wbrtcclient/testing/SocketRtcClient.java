package io.github.iamrajendra.apprtcsocket.wbrtcclient.testing;
import android.app.Activity;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by gwl on 17/7/17.
 */

public class SocketRtcClient {
    private final MediaConstraints pcConstraints;
    private String TAG = SocketRtcClient.class.getSimpleName();
    private String address;
    private PeerConnection peerConnection;
    private Socket client;
    private SSLContext sc;
    private static final String LOCAL_MEDIA_STREAM_ID = "streamId";
    private Activity activity;
    private String remoteUserId;
    public interface WebRTCSocektListener
    {
        public void onAddStream(MediaStream stream);
    }

    private WebRTCSocektListener webRTCSocektListener;
    MediaStream mMediaStream;
    public SocketRtcClient(MediaStream mediaStream,Activity activity, String address, LinkedList<PeerConnection.IceServer> iceServers, final PeerConnectionFactory peerConnectionFactory,WebRTCSocektListener listener) {
        this.address = address;
        this.webRTCSocektListener  = listener;
        this.mMediaStream=mediaStream;
       // MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);
        this.activity = activity;
        pcConstraints = new MediaConstraints();
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, pcConstraints, new PeerConnection.Observer() {
                    @Override
                    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                        Log.i(TAG, "onSignalingChange: create offer " + signalingState);
                    }
                    @Override
                    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                        Log.i(TAG, "onIceConnectionChange:  create offer " + iceConnectionState);
                    }
                    @Override
                    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                        Log.i(TAG, "onIceGatheringChange: create offer " + iceGatheringState);
                    }
                    @Override
                    public void onIceCandidate(IceCandidate iceCandidate) {
                        Log.i(TAG, "onIceCandidate(sdp): " + iceCandidate.sdp);
                        Log.i(TAG, "onIceCandidate: (sdpMid)" + iceCandidate.sdpMid);
                        Log.i(TAG, "onIceCandidate: (sdpMLineIndex)" + iceCandidate.sdpMLineIndex);
                        JSONObject json = new JSONObject();
                        try {
                            json.put("type", "candidate");
                            json.put("label", iceCandidate.sdpMLineIndex);
                            json.put("id", iceCandidate.sdpMid);
                            json.put("candidate", iceCandidate.sdp);
                            sendMessage(remoteUserId, json);
                        } catch (JSONException e) {
                            Log.i(TAG, "onIceCandidate: "+e.toString());
                        }
                    }
                    @Override
                    public void onAddStream(MediaStream mediaStream) {
                        Log.i(TAG, "onAddStream: " + mediaStream);
//                        peerConnection.addStream(mediaStream);
                        webRTCSocektListener.onAddStream(mediaStream);
                    }

                    @Override
                    public void onRemoveStream(MediaStream mediaStream) {
                        Log.i(TAG, "onRemoveStream: " + mediaStream);
                    }

                    @Override
                    public void onDataChannel(DataChannel dataChannel) {
                        Log.i(TAG, "onDataChannel: " + dataChannel);
                    }

                    @Override
                    public void onRenegotiationNeeded() {
                        Log.i(TAG, "onRenegotiationNeeded: ");

                    }
                });
        peerConnection.addStream(mediaStream);

        try {

            connectSocket();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private class OfferSDP implements SdpObserver {
        private String id;
        private JSONObject connectionDescription;

        public OfferSDP(String id, JSONObject connectionDescription) {
            this.id = id;
            this.connectionDescription = connectionDescription;

        }

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "onCreateSuccess:   offer sdp " + sdp);
                    JSONObject payload = new JSONObject();
                    try {
                        JSONObject remotePeerSdpContraints = new JSONObject();
                        JSONObject extra = new JSONObject();

                        remotePeerSdpContraints.put("OfferToReceiveAudio", true);
                        remotePeerSdpContraints.put("OfferToReceiveVideo", true);

                        payload.put("type", sdp.type.canonicalForm());
                        payload.put("sdp", sdp.description);
                        payload.put("remotePeerSdpConstraints", remotePeerSdpContraints);
                        payload.put("renegotiatingPeer", false);
                        payload.put("connectionDescription", connectionDescription);
                        payload.put("dontGetRemoteStream", false);
                        payload.put("extra", extra);

                        JSONObject streamsToShare = new JSONObject();

//                        JSONObject jsonObject=new JSONObject("1fa2d6da-0d8f-4ac4-a792-585edef5a0df");
//                        jsonObject.put("","1fa2d6da-0d8f-4ac4-a792-585edef5a0df");
                        JSONObject jsonObject = new JSONObject();

//                        streamsToShare.put("",jsonObject);

                        streamsToShare.put("isAudio", false);
                        streamsToShare.put("isVideo", true);
                        streamsToShare.put("isScreen", false);
                        jsonObject.put("streamId", streamsToShare);


                        payload.put("streamsToShare", jsonObject);
                        payload.put("isFirefoxOffered", true);
//                        Log.i(TAG, "run:message  "+streamsToShare.toString());
                        sendMessage(id, payload);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreateSuccess: SDP " + e);
                    }
                    peerConnection.setLocalDescription(OfferSDP.this, sdp);

                }
            });

        }

        @Override
        public void onSetSuccess() {
            Log.i(TAG, "onSetSuccess: offer create");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure: offer is not created reson" + s);

        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure:  offer is not set reson" + s);
        }
    }

    private class AnswerSDP implements SdpObserver {
        private String id;
        private JSONObject connectionDescription;

        public AnswerSDP(String id, JSONObject connectionDescription) {
            this.id = id;
            this.connectionDescription = connectionDescription;

        }

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "onCreateSuccess:   answer sdp " + sdp);
                    JSONObject payload = new JSONObject();
                    try {
                        JSONObject remotePeerSdpContraints = new JSONObject();
                        JSONObject extra = new JSONObject();

                        remotePeerSdpContraints.put("OfferToReceiveAudio", true);
                        remotePeerSdpContraints.put("OfferToReceiveVideo", true);

                        payload.put("type", sdp.type.canonicalForm());
                        payload.put("sdp", sdp.description);
                        payload.put("remotePeerSdpConstraints", remotePeerSdpContraints);
                        payload.put("renegotiatingPeer", false);
                        payload.put("connectionDescription", connectionDescription);
                        payload.put("dontGetRemoteStream", false);
                        payload.put("extra", extra);

                        JSONObject streamsToShare = new JSONObject();

//                        JSONObject jsonObject=new JSONObject("1fa2d6da-0d8f-4ac4-a792-585edef5a0df");
//                        jsonObject.put("","1fa2d6da-0d8f-4ac4-a792-585edef5a0df");
                        JSONObject jsonObject = new JSONObject();

//                        streamsToShare.put("",jsonObject);

                        streamsToShare.put("isAudio", false);
                        streamsToShare.put("isVideo", true);
                        streamsToShare.put("isScreen", false);

                        jsonObject.put("streamId", streamsToShare);

                        payload.put("streamsToShare", jsonObject);
                        payload.put("isFirefoxOffered", true);
//                        Log.i(TAG, "run:message  "+streamsToShare.toString());
                        sendMessage(id, payload);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreateSuccess: SDP " + e);
                    }
                    peerConnection.setLocalDescription(AnswerSDP.this, sdp);

                }
            });

        }

        @Override
        public void onSetSuccess() {
            Log.i(TAG, "onSetSuccess: offer create");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure: offer is not created reson" + s);

        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure:  offer is not set reson" + s);
        }
    }
    public void sendMessage(String remoteUserId, JSONObject payload) throws JSONException {
        Log.i(TAG, "sendMessage: sending local sdp type" + "payload" + payload);
        JSONObject message = new JSONObject();
        message.put("remoteUserId", remoteUserId);
        message.put("sender", "raj");
        message.put("message", payload);
        client.emit("video-conference-demo", message);
    }

    private void connectSocket() throws URISyntaxException {
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = false;
        options.secure = true;
        options.sslContext = sc;
        options.query = "socketCustomEvent=RTCMultiConnection-Custom-Message&sessionid=raj&userid=raj&autoCloseEntireSession=false&msgEvent=video-conference-demo&maxParticipantsAllowed=1000&log=true&EIO:3&transport:polling&t:LqiYB5k";

        client = IO.socket("https://192.168.7.129:31506", options);


        client.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {


                Log.i(TAG, "call: connect" + args.length);
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "call: event connect + EVENT_CONNECT_ERROR " + args[0]);

            }
        }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "call:  event connect timeout");
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "call:  event error" + args[0]);
            }
        }).on("video-conference-demo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "call: remoteSDP" + args[0]);
                final JSONObject mainjson = (JSONObject) args[0];
                try {
                    remoteUserId = mainjson.getString("sender");
                    JSONObject message = mainjson.getJSONObject("message");



                    if(message.has("candidate")  )
                    {
                        Log.i(TAG, "adding ice candidate ");

                        IceCandidate candidate = new IceCandidate(
                                message.getString("sdpMid"),
                                message.getInt("sdpMLineIndex"),
                                message.getString("candidate"));
                        peerConnection.addIceCandidate(candidate);


                    }



                    if (message.has("newParticipationRequest")&& message.getBoolean("newParticipationRequest")) {
                        Log.i(TAG, "call: creating offer");
                        peerConnection.createOffer(new OfferSDP(mainjson.getString("sender"), mainjson), pcConstraints);
                    } else {

                        if (message.has("type") && message.getString("type").equals("answer")) {
                            Log.i(TAG, "accept answer ");

                            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, message.getString("sdp"));

                            peerConnection.setRemoteDescription(new SdpObserver() {
                                @Override
                                public void onCreateSuccess(SessionDescription sessionDescription) {
                                    Log.i(TAG, "onCreateSuccess: answer remote des");
                                    try {

                                        peerConnection.createAnswer(new AnswerSDP(mainjson.getString("sender"), mainjson), pcConstraints);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onSetSuccess() {
                                    Log.i(TAG, "onSetSuccess: answer remote des");

                                }

                                @Override
                                public void onCreateFailure(String s) {
                                    Log.e(TAG, "onCreateFailure:  answer remote des" + s);

                                }

                                @Override
                                public void onSetFailure(String s) {
                                    Log.e(TAG, "onSetFailure: answer remote des" + s);

                                }
                            }, sessionDescription);
                        }



                    }




                } catch (JSONException e) {
                    Log.e(TAG, "call: exception"+e );
                }
            }
        });
        if (client != null) {

            client.connect();
        } else {
            Log.d(TAG, "Socket Connected");
        }

    }


    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.i(TAG, "checkClientTrusted: " + authType + " " + chain);
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.i(TAG, "checkServerTrusted: " + authType + " " + chain);
        }
    }
    };


}