package droidkit.processor.sqlite;

import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
abstract class ConflictResolution {

    private static final List<String> SUPPORTED = Arrays.asList(
            "",
            " ON CONFLICT ROLLBACK",
            " ON CONFLICT ABORT",
            " ON CONFLICT FAIL",
            " ON CONFLICT IGNORE",
            " ON CONFLICT REPLACE"
    );

    private ConflictResolution() {
        //no instance
    }

    public static String get(int clause) {
        return SUPPORTED.get(Math.min(SUPPORTED.size() - 1, Math.max(0, clause)));
    }

    /*

    private static class OnConflictRollback implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT ROLLBACK";
        }
    }

    private static class OnConflictAbort implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT ABORT";
        }
    }

    private static class OnConflictFail implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT FAIL";
        }
    }

    private static class OnConflictIgnore implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT IGNORE";
        }
    }

    private static class OnConflictReplace implements ConflictResolution {
        @Override
        public String call() {
            return " ON CONFLICT REPLACE";
        }
    }
     */

}
