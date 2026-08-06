package com.quantferox.lumeo.validation;

import jakarta.validation.groups.Default;

/**
 * Validation group marker for UPDATE operations.
 * Extends {@link Default} so all default constraints still apply.
 *
 * Usage in controller: {@code @Validated(OnUpdate.class)}
 */
public interface OnUpdate extends Default {
}
