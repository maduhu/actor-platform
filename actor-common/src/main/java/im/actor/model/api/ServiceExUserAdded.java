package im.actor.model.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;

public class ServiceExUserAdded extends BserObject {

    private int addedUid;

    public ServiceExUserAdded(int addedUid) {
        this.addedUid = addedUid;
    }

    public ServiceExUserAdded() {

    }

    public int getAddedUid() {
        return this.addedUid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.addedUid = values.getInt(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.addedUid);
    }

}
