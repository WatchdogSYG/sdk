package flinderstemi.util;

import com.robotemi.sdk.TtsRequest;

import java.util.List;

public class TTSSequence {
    private List<TtsRequest> ttss;

    public TTSSequence(List<TtsRequest> ttsReqs) {
        ttss = ttsReqs;
    }
}
