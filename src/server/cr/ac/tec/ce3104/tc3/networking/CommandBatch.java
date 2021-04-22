package cr.ac.tec.ce3104.tc3.networking;

import java.util.List;
import java.util.ArrayList;

public class CommandBatch {
    @Override
    public String toString() {
        if (this.memoizedString == null) {
            this.memoizedString = "";
            for (Command command : this.batch) {
                this.memoizedString += command.toString() + "\n";
            }
        }

        return this.memoizedString;
    }

    public void clear() {
        this.batch.clear();
        this.memoizedString = null;
    }

    public void add(Command command) {
        this.batch.add(command);
        this.memoizedString = null;
    }

    private List<Command> batch = new ArrayList<>();
    private String memoizedString = null;
}
