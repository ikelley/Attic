/*
 * Copyright 2004 - 2012 Cardiff University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atticfs.util;

import org.atticfs.types.Constraint;

import java.util.regex.Pattern;

/**
 * Numeric operators return false if the constraint type of the first argument is not numeric, i.e. String or boolean
 *
 * 
 */

public class ConstraintMatcher {

    /**
     * checks if the seconds Constraint argument matches the first.
     * If the values of both are null, this returns true.
     * If the first constraint is NOT null, and the second is, this also returns true.
     * In other words an undefined constraint is equal to a match.
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean matches(Constraint c1, Constraint c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.getValue() == null && c2.getValue() == null) {
            return true;
        }
        if ((c1 == null || c1.getValue() == null) && (c2 != null && c2.getValue() != null)) {
            return false;
        }
        if ((c1 != null && c1.getValue() != null) && (c2 == null || c2.getValue() == null)) {
            return true;
        }


        Constraint.Type type1 = c1.getConstraintType();

        if (type1 == Constraint.Type.Boolean) {
            if (c1.getBooleanValue() == c2.getBooleanValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Integer) {
            if (c1.getIntegerValue() == c2.getIntegerValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Long) {
            if (c1.getLongValue() == c2.getLongValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Double) {
            if (c1.getDoubleValue() == c2.getDoubleValue()) {
                return true;
            }
        } else {
            return Pattern.matches(c2.getValue(), c1.getValue());
        }
        return false;
    }

    public static boolean isLessThan(Constraint c1, Constraint c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.getValue() == null && c2.getValue() == null) {
            return true;
        }
        if ((c1 == null || c1.getValue() == null) && (c2 != null && c2.getValue() != null)) {
            return false;
        }
        if ((c1 != null && c1.getValue() != null) && (c2 == null || c2.getValue() == null)) {
            return true;
        }

        Constraint.Type type1 = c1.getConstraintType();

        if (type1 == Constraint.Type.Boolean) {
            return false;
        } else if (type1 == Constraint.Type.Integer) {
            if (c1.getIntegerValue() < c2.getIntegerValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Long) {
            if (c1.getLongValue() < c2.getLongValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Double) {
            if (c1.getDoubleValue() < c2.getDoubleValue()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGreaterThan(Constraint c1, Constraint c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.getValue() == null && c2.getValue() == null) {
            return true;
        }
        if ((c1 == null || c1.getValue() == null) && (c2 != null && c2.getValue() != null)) {
            return false;
        }
        if ((c1 != null && c1.getValue() != null) && (c2 == null || c2.getValue() == null)) {
            return true;
        }

        Constraint.Type type1 = c1.getConstraintType();

        if (type1 == Constraint.Type.Boolean) {
            return false;
        } else if (type1 == Constraint.Type.Integer) {
            if (c1.getIntegerValue() > c2.getIntegerValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Long) {
            if (c1.getLongValue() > c2.getLongValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Double) {
            if (c1.getDoubleValue() > c2.getDoubleValue()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLessThanOrEqual(Constraint c1, Constraint c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.getValue() == null && c2.getValue() == null) {
            return true;
        }
        if ((c1 == null || c1.getValue() == null) && (c2 != null && c2.getValue() != null)) {
            return false;
        }
        if ((c1 != null && c1.getValue() != null) && (c2 == null || c2.getValue() == null)) {
            return true;
        }

        Constraint.Type type1 = c1.getConstraintType();

        if (type1 == Constraint.Type.Boolean) {
            return false;
        } else if (type1 == Constraint.Type.Integer) {
            if (c1.getIntegerValue() <= c2.getIntegerValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Long) {
            if (c1.getLongValue() <= c2.getLongValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Double) {
            if (c1.getDoubleValue() <= c2.getDoubleValue()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGreaterThanOrEqual(Constraint c1, Constraint c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.getValue() == null && c2.getValue() == null) {
            return true;
        }
        if ((c1 == null || c1.getValue() == null) && (c2 != null && c2.getValue() != null)) {
            return false;
        }
        if ((c1 != null && c1.getValue() != null) && (c2 == null || c2.getValue() == null)) {
            return true;
        }

        Constraint.Type type1 = c1.getConstraintType();

        if (type1 == Constraint.Type.Boolean) {
            return false;
        } else if (type1 == Constraint.Type.Integer) {
            if (c1.getIntegerValue() >= c2.getIntegerValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Long) {
            if (c1.getLongValue() >= c2.getLongValue()) {
                return true;
            }
        } else if (type1 == Constraint.Type.Double) {
            if (c1.getDoubleValue() >= c2.getDoubleValue()) {
                return true;
            }
        }
        return false;
    }

}
