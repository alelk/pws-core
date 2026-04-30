package io.github.alelk.pws.features.resources

import io.github.alelk.pws.domain.tonality.Tonality
import org.jetbrains.compose.resources.StringResource

val Tonality.label: StringResource
  get() = when (this) {
    Tonality.A_MAJOR -> Res.string.tonality_a_major
    Tonality.A_MINOR -> Res.string.tonality_a_minor
    Tonality.A_FLAT_MAJOR -> Res.string.tonality_a_flat_major
    Tonality.A_FLAT_MINOR -> Res.string.tonality_a_flat_minor
    Tonality.A_SHARP_MINOR -> Res.string.tonality_a_sharp_minor
    Tonality.B_MAJOR -> Res.string.tonality_b_major
    Tonality.B_MINOR -> Res.string.tonality_b_minor
    Tonality.B_FLAT_MAJOR -> Res.string.tonality_b_flat_major
    Tonality.B_FLAT_MINOR -> Res.string.tonality_b_flat_minor
    Tonality.C_MAJOR -> Res.string.tonality_c_major
    Tonality.C_MINOR -> Res.string.tonality_c_minor
    Tonality.C_FLAT_MAJOR -> Res.string.tonality_c_flat_major
    Tonality.C_SHARP_MAJOR -> Res.string.tonality_c_sharp_major
    Tonality.C_SHARP_MINOR -> Res.string.tonality_c_sharp_minor
    Tonality.D_MAJOR -> Res.string.tonality_d_major
    Tonality.D_MINOR -> Res.string.tonality_d_minor
    Tonality.D_FLAT_MAJOR -> Res.string.tonality_d_flat_major
    Tonality.D_SHARP_MINOR -> Res.string.tonality_d_sharp_minor
    Tonality.E_MAJOR -> Res.string.tonality_e_major
    Tonality.E_MINOR -> Res.string.tonality_e_minor
    Tonality.E_FLAT_MAJOR -> Res.string.tonality_e_flat_major
    Tonality.E_FLAT_MINOR -> Res.string.tonality_e_flat_minor
    Tonality.F_MAJOR -> Res.string.tonality_f_major
    Tonality.F_MINOR -> Res.string.tonality_f_minor
    Tonality.F_SHARP_MAJOR -> Res.string.tonality_f_sharp_major
    Tonality.F_SHARP_MINOR -> Res.string.tonality_f_sharp_minor
    Tonality.G_MAJOR -> Res.string.tonality_g_major
    Tonality.G_MINOR -> Res.string.tonality_g_minor
    Tonality.G_FLAT_MAJOR -> Res.string.tonality_g_flat_major
    Tonality.G_SHARP_MINOR -> Res.string.tonality_g_sharp_minor
  }
