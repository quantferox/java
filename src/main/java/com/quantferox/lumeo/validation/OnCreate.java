package com.quantferox.lumeo.validation;

import jakarta.validation.groups.Default;

/**
 * Validation group marker for CREATE operations.
 * Extends {@link Default} so all default constraints still apply.
 *
 * Usage in controller: {@code @Validated(OnCreate.class)}
 */
public interface OnCreate extends Default {
}
