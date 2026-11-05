package com.cocido.oficios.domain.validation

import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.model.WeeklyAvailability

object ScheduleValidator {
    const val EMPTY_WEEK = "Seleccione al menos un día laboral"
    const val FROM_AFTER_TO = "La hora de fin debe ser posterior al inicio."
    const val OVERLAP = "Las franjas de un mismo día no pueden superponerse."

    fun validate(week: WeeklyAvailability): String? {
        if (week.activeDays.isEmpty()) return EMPTY_WEEK
        return TimeRangeValidation.validateWeek(week)
    }

    fun validateRange(range: TimeRange): String? = TimeRangeValidation.validateRange(range)
}
