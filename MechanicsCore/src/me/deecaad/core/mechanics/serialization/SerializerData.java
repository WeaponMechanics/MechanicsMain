package me.deecaad.core.mechanics.serialization;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializerData {
    
    /**
     * @return Nonnull name/key of the serializer
     */
    @Nonnull
    String name();
    
    /**
     * Since annotations cannot use <code>Object</code> types, we
     * have to use strings as an <code>Argument</code>.
     *
     * The format is as follows: <code>name~type~alias1,alias2</code>
     * If there are no aliases to the argument, you do not need to specify.
     * Example: <code>name~type</code>
     *
     * The name is the name of the argument. For potions, for example, this
     * would be the potion's type, or duration, or level. For datatypes,
     * this would be STRING/POTION, INTEGER, INTEGER, respectively.
     * @see me.deecaad.core.mechanics.types.PotionMechanic
     *
     * If you need a custom datatype, you need to register a <code>DataType</code>.
     * This is done automatically as you instantiate a <code>DataType</code>. There
     * are premade classes for enums/serializers, make sure to look at those before
     * making your own datatypes.
     * @see me.deecaad.core.mechanics.serialization.datatypes.EnumType
     * @see me.deecaad.core.mechanics.serialization.datatypes.SerializerType
     * @see me.deecaad.core.mechanics.serialization.datatypes.DataType
     *
     * @return Array of arguments
     */
    @Nonnull
    String[] args() default {};
}
