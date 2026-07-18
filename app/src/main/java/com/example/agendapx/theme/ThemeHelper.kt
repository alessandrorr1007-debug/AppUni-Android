package com.example.agendapx.theme

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.agendapx.R

object ThemeHelper {

    fun color(context: Context, @ColorRes resId: Int): Int =
        ContextCompat.getColor(context, resId)

    fun colorBackground(context: Context): Int = color(context, R.color.color_background)
    fun colorSurface(context: Context): Int = color(context, R.color.color_surface)
    fun colorSurfaceVariant(context: Context): Int = color(context, R.color.color_surface_variant)
    fun colorSurfaceElevated(context: Context): Int = color(context, R.color.color_surface_elevated)
    fun colorPrimary(context: Context): Int = color(context, R.color.color_primary)
    fun colorPrimaryContainer(context: Context): Int = color(context, R.color.color_primary_container)
    fun colorOnPrimary(context: Context): Int = color(context, R.color.color_on_primary)
    fun colorOnPrimaryContainer(context: Context): Int = color(context, R.color.color_on_primary_container)
    fun colorSecondary(context: Context): Int = color(context, R.color.color_secondary)
    fun colorSecondaryContainer(context: Context): Int = color(context, R.color.color_secondary_container)
    fun colorOnSecondary(context: Context): Int = color(context, R.color.color_on_secondary)
    fun colorTertiary(context: Context): Int = color(context, R.color.color_tertiary)
    fun colorSuccess(context: Context): Int = color(context, R.color.color_success)
    fun colorWarning(context: Context): Int = color(context, R.color.color_warning)
    fun colorError(context: Context): Int = color(context, R.color.color_error)
    fun colorInfo(context: Context): Int = color(context, R.color.color_info)
    fun colorTextPrimary(context: Context): Int = color(context, R.color.color_text_primary)
    fun colorTextSecondary(context: Context): Int = color(context, R.color.color_text_secondary)
    fun colorTextMuted(context: Context): Int = color(context, R.color.color_text_muted)
    fun colorTextOnPrimary(context: Context): Int = color(context, R.color.color_text_on_primary)
    fun colorBorder(context: Context): Int = color(context, R.color.color_border)
    fun colorDivider(context: Context): Int = color(context, R.color.color_divider)
    fun colorInputBackground(context: Context): Int = color(context, R.color.color_input_background)
    fun colorInputBorder(context: Context): Int = color(context, R.color.color_input_border)
    fun colorInputFocus(context: Context): Int = color(context, R.color.color_input_focus)
    fun colorBottomNavBg(context: Context): Int = color(context, R.color.color_bottom_nav_bg)
    fun colorBottomNavActive(context: Context): Int = color(context, R.color.color_bottom_nav_active)
    fun colorBottomNavInactive(context: Context): Int = color(context, R.color.color_bottom_nav_inactive)
    fun colorCardBg(context: Context): Int = color(context, R.color.color_card_bg)
    fun colorCardHeader(context: Context): Int = color(context, R.color.color_card_header)
    fun colorGradeApproved(context: Context): Int = color(context, R.color.color_grade_approved)
    fun colorGradeApprovedLight(context: Context): Int = color(context, R.color.color_grade_approved_light)
    fun colorGradeRisk(context: Context): Int = color(context, R.color.color_grade_risk)
    fun colorGradeRiskLight(context: Context): Int = color(context, R.color.color_grade_risk_light)
    fun colorGradeFail(context: Context): Int = color(context, R.color.color_grade_fail)
    fun colorGradeFailLight(context: Context): Int = color(context, R.color.color_grade_fail_light)
    fun colorGradePending(context: Context): Int = color(context, R.color.color_grade_pending)
}
