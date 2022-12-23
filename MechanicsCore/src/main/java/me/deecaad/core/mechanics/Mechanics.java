package me.deecaad.core.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class Mechanics implements Serializer<Mechanics> {

    private static final Mechanic DUMMY = new Mechanic() {
        @Override
        protected void use0(CastData cast) {}

        @Override
        public String getKeyword() {
            return null;
        }
    };

    public static final Registry<Mechanic> MECHANICS = new Registry<>();
    public static final Registry<Targeter> TARGETERS = new Registry<>();
    public static final Registry<Condition> CONDITIONS = new Registry<>();

    private List<Mechanic> mechanics;

    /**
     * Default constructor for serializer.
     */
    public Mechanics() {
    }

    public Mechanics(List<Mechanic> mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    public void use(CastData cast) {
        for (Mechanic mechanic : mechanics) {
            mechanic.use(cast);
        }
    }

    @NotNull
    @Override
    public Mechanics serialize(SerializeData data) throws SerializerException {
        List<Mechanic> mechanics = new LinkedList<>();

        if (data.config.isConfigurationSection(data.key)) {

        } else if (data.config.isList(data.key)) {
            for (String line : data.config.getStringList(data.key)) {
                try {
                    mechanics.add(DUMMY.inlineFormat(line));
                } catch (InlineException ex) {
                    boolean isIndexAccurate = true;
                    int index = ex.getIndex();
                    if (index == -1) {
                        if (ex.getLookAfter() != null) index = line.indexOf(ex.getLookAfter());
                        index = line.indexOf(ex.getIssue(), index == -1 ? 0 : index);
                        isIndexAccurate = false;
                    }

                    String prefix = isIndexAccurate ? "Error happened here: " : "Error might be here: ";
                    ex.getException().addMessage(prefix + line);
                    if (index != -1) ex.getException().addMessage(StringUtil.repeat(" ", index + prefix.length()) + "^");

                    throw ex.getException();
                }
            }
        }

        // Extra check to see if the user has extra mechanics in config, when
        // it is better to leave it empty (So the config doesn't store anything).
        if (mechanics.isEmpty())
            throw data.exception(null, "Found an empty list of Mechanics, was this intentional?",
                    "Instead of using an empty list, please delete the option from config");

        return new Mechanics(mechanics);
    }
}