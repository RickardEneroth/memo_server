package xyz.eneroth.memo;

import java.util.ArrayList;
import java.util.List;

public class MemoResponse {
    public List<MemoRecord> memos = new ArrayList();

    public List getMemos() {
        return memos;
    }

    public void setMemos(List memos) {
        this.memos = memos;
    }
}
