package me.deecaad.core.file.inline;

import me.deecaad.core.file.inline.types.IntegerType;
import me.deecaad.core.file.inline.types.NestedType;

import java.util.Map;

public class FooSerializer extends InlineSerializer<FooSerializer> {

    public static final Argument NUM = new Argument("num", new IntegerType(1), 1);
    public static final Argument NESTED = new Argument("nested", new NestedType<>(NestedSerializer.class));

    private int num;
    private NestedSerializer nested;

    public FooSerializer() {
    }

    public FooSerializer(Map<Argument, Object> args) {
        num = (int) args.get(NUM);
        nested = (NestedSerializer) args.get(NESTED);
    }

    public int getNum() {
        return num;
    }

    public NestedSerializer getNested() {
        return nested;
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap(NUM, NESTED);
    }

    @Override
    public String getKeyword() {
        return "Foo";
    }


    public static class NestedSerializer extends InlineSerializer<NestedSerializer> {

        public static final Argument HEY = new Argument("hey", new IntegerType(0), 0);

        private int hey;

        public NestedSerializer() {
        }

        public NestedSerializer(Map<Argument, Object> args) {
            hey = (int) args.get(HEY);
        }

        public int getHey() {
            return hey;
        }

        @Override
        public ArgumentMap args() {
            return new ArgumentMap(HEY);
        }

        @Override
        public String getKeyword() {
            return "Nested";
        }
    }
}
