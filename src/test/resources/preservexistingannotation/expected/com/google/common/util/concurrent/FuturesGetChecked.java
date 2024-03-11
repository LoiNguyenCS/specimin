package com.google.common.util.concurrent;

final class FuturesGetChecked {

    interface GetCheckedTypeValidator {
    }

    static GetCheckedTypeValidator weakSetValidator() {
        throw new Error();
    }

    static class GetCheckedTypeValidatorHolder {

        static final String CLASS_VALUE_VALIDATOR_NAME = null;

        @IgnoreJRERequirement
        enum ClassValueValidator implements GetCheckedTypeValidator {

            INSTANCE
        }

        enum WeakSetValidator implements GetCheckedTypeValidator {

            INSTANCE
        }

        static GetCheckedTypeValidator getBestValidator() {
            try {
                Class<?> theClass = Class.forName(CLASS_VALUE_VALIDATOR_NAME);
                return (GetCheckedTypeValidator) theClass.getEnumConstants()[0];
            } catch (Throwable t) {
                return weakSetValidator();
            }
        }
    }
}
