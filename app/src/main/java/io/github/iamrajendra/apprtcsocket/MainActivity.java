package io.github.iamrajendra.apprtcsocket;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String VIDEO_TRACK_ID ="io.github.iamrajendra.apprtcsocket.videotrack" ;
    private static final String AUDIO_TRACK_ID = "io.github.iamrajendra.apprtcsocket.audiotrack";
    private static final String LOCAL_MEDIA_STREAM_ID = "io.github.iamrajendra.apprtcsocket.audiotrack.localstreamid";
    ;
    private Context mContext;
    private boolean initializeAudio = true;
    private boolean initializeVideo = true;
    private boolean videoCodecHwAcceleration = true;
    private PeerConnectionFactory mPeerConnectionFactory;
    private MediaConstraints mConstraintsVideo;
    private MediaConstraints audioConstraints;
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView vsv;
    private MediaConstraints videoConstraints;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.content_main);
        videoConstraints = new MediaConstraints();
        audioConstraints = new MediaConstraints();
        PeerConnectionFactory.initializeAndroidGlobals(
                this,
                initializeAudio,
                initializeVideo,
                videoCodecHwAcceleration,
                null);




        PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory();
        VideoCapturerAndroid capturer = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice());
//        / First we create a VideoSource
        VideoSource videoSource =
                peerConnectionFactory.createVideoSource(capturer, videoConstraints);
// First we create an AudioSource
        AudioSource audioSource =
                peerConnectionFactory.createAudioSource(audioConstraints);

// Once we have that, we can create our AudioTrack
// Note that AUDIO_TRACK_ID can be any string that uniquely
// identifies that audio track in your application
        AudioTrack localAudioTrack =
                peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
// Once we have that, we can create our VideoTrack
// Note that VIDEO_TRACK_ID can be any string that uniquely
// identifies that video track in your application
        VideoTrack localVideoTrack =
                peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {

            }
        });

        // Then we set that view, and pass a Runnable
// to run once the surface is ready

// Now that VideoRendererGui is ready, we can get our VideoRenderer
        VideoRenderer renderer = null;
        try {
            renderer = VideoRendererGui.createGui(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        } catch (Exception e) {
            Log.e(TAG, "onCreate:  render error" + e);
        }

// And finally, with our VideoRenderer ready, we
// can add our renderer to the VideoTrack.
        localVideoTrack.addRenderer(renderer);
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);
        // Now we can add our tracks.
        mediaStream.addTrack(localVideoTrack);
        mediaStream.addTrack(localAudioTrack);

        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        MediaConstraints  pcConstraints  = new MediaConstraints();
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                pcConstraints,
                new PeerConnection.Observer() {
                    @Override
                    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

                    }

                    @Override
                    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

                    }

                    @Override
                    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

                    }

                    @Override
                    public void onIceCandidate(IceCandidate iceCandidate) {

                    }

                    @Override
                    public void onAddStream(MediaStream mediaStream) {

                    }

                    @Override
                    public void onRemoveStream(MediaStream mediaStream) {

                    }

                    @Override
                    public void onDataChannel(DataChannel dataChannel) {

                    }

                    @Override
                    public void onRenegotiationNeeded() {

                    }
                });

    }

}
