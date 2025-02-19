/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/* This file was automatically generated by CasADi 3.6.7.
 *  It consists of:
 *   1) content generated by CasADi runtime: not copyrighted
 *   2) template code copied from CasADi source: permissively licensed (MIT-0)
 *   3) user code: owned by the user
 *
 */
#ifdef __cplusplus
extern "C" {
#endif

#ifndef GH_1682_PHOTON_TARGETING_SRC_MAIN_NATIVE_CPP_PHOTON_CONSTRAINED_SOLVEPNP_GENERATE_CONSTRAINED_SOLVEPNP_5_TAGS_FIXED_H_
#define GH_1682_PHOTON_TARGETING_SRC_MAIN_NATIVE_CPP_PHOTON_CONSTRAINED_SOLVEPNP_GENERATE_CONSTRAINED_SOLVEPNP_5_TAGS_FIXED_H_ \
  double
#endif  // GH_1682_PHOTON_TARGETING_SRC_MAIN_NATIVE_CPP_PHOTON_CONSTRAINED_SOLVEPNP_GENERATE_CONSTRAINED_SOLVEPNP_5_TAGS_FIXED_H_

#ifndef casadi_int
#define casadi_int long long int
#endif

int calc_J_5_tags_heading_fixed(const casadi_real** arg, casadi_real** res,
                                casadi_int* iw, casadi_real* w, int mem);
int calc_J_5_tags_heading_fixed_alloc_mem(void);
int calc_J_5_tags_heading_fixed_init_mem(int mem);
void calc_J_5_tags_heading_fixed_free_mem(int mem);
int calc_J_5_tags_heading_fixed_checkout(void);
void calc_J_5_tags_heading_fixed_release(int mem);
void calc_J_5_tags_heading_fixed_incref(void);
void calc_J_5_tags_heading_fixed_decref(void);
casadi_int calc_J_5_tags_heading_fixed_n_in(void);
casadi_int calc_J_5_tags_heading_fixed_n_out(void);
casadi_real calc_J_5_tags_heading_fixed_default_in(casadi_int i);
const char* calc_J_5_tags_heading_fixed_name_in(casadi_int i);
const char* calc_J_5_tags_heading_fixed_name_out(casadi_int i);
const casadi_int* calc_J_5_tags_heading_fixed_sparsity_in(casadi_int i);
const casadi_int* calc_J_5_tags_heading_fixed_sparsity_out(casadi_int i);
int calc_J_5_tags_heading_fixed_work(casadi_int* sz_arg, casadi_int* sz_res,
                                     casadi_int* sz_iw, casadi_int* sz_w);
int calc_J_5_tags_heading_fixed_work_bytes(casadi_int* sz_arg,
                                           casadi_int* sz_res,
                                           casadi_int* sz_iw, casadi_int* sz_w);
#define calc_J_5_tags_heading_fixed_SZ_ARG 12
#define calc_J_5_tags_heading_fixed_SZ_RES 1
#define calc_J_5_tags_heading_fixed_SZ_IW 0
#define calc_J_5_tags_heading_fixed_SZ_W 113
int calc_gradJ_5_tags_heading_fixed(const casadi_real** arg, casadi_real** res,
                                    casadi_int* iw, casadi_real* w, int mem);
int calc_gradJ_5_tags_heading_fixed_alloc_mem(void);
int calc_gradJ_5_tags_heading_fixed_init_mem(int mem);
void calc_gradJ_5_tags_heading_fixed_free_mem(int mem);
int calc_gradJ_5_tags_heading_fixed_checkout(void);
void calc_gradJ_5_tags_heading_fixed_release(int mem);
void calc_gradJ_5_tags_heading_fixed_incref(void);
void calc_gradJ_5_tags_heading_fixed_decref(void);
casadi_int calc_gradJ_5_tags_heading_fixed_n_in(void);
casadi_int calc_gradJ_5_tags_heading_fixed_n_out(void);
casadi_real calc_gradJ_5_tags_heading_fixed_default_in(casadi_int i);
const char* calc_gradJ_5_tags_heading_fixed_name_in(casadi_int i);
const char* calc_gradJ_5_tags_heading_fixed_name_out(casadi_int i);
const casadi_int* calc_gradJ_5_tags_heading_fixed_sparsity_in(casadi_int i);
const casadi_int* calc_gradJ_5_tags_heading_fixed_sparsity_out(casadi_int i);
int calc_gradJ_5_tags_heading_fixed_work(casadi_int* sz_arg, casadi_int* sz_res,
                                         casadi_int* sz_iw, casadi_int* sz_w);
int calc_gradJ_5_tags_heading_fixed_work_bytes(casadi_int* sz_arg,
                                               casadi_int* sz_res,
                                               casadi_int* sz_iw,
                                               casadi_int* sz_w);
#define calc_gradJ_5_tags_heading_fixed_SZ_ARG 12
#define calc_gradJ_5_tags_heading_fixed_SZ_RES 1
#define calc_gradJ_5_tags_heading_fixed_SZ_IW 0
#define calc_gradJ_5_tags_heading_fixed_SZ_W 254
int calc_hessJ_5_tags_heading_fixed(const casadi_real** arg, casadi_real** res,
                                    casadi_int* iw, casadi_real* w, int mem);
int calc_hessJ_5_tags_heading_fixed_alloc_mem(void);
int calc_hessJ_5_tags_heading_fixed_init_mem(int mem);
void calc_hessJ_5_tags_heading_fixed_free_mem(int mem);
int calc_hessJ_5_tags_heading_fixed_checkout(void);
void calc_hessJ_5_tags_heading_fixed_release(int mem);
void calc_hessJ_5_tags_heading_fixed_incref(void);
void calc_hessJ_5_tags_heading_fixed_decref(void);
casadi_int calc_hessJ_5_tags_heading_fixed_n_in(void);
casadi_int calc_hessJ_5_tags_heading_fixed_n_out(void);
casadi_real calc_hessJ_5_tags_heading_fixed_default_in(casadi_int i);
const char* calc_hessJ_5_tags_heading_fixed_name_in(casadi_int i);
const char* calc_hessJ_5_tags_heading_fixed_name_out(casadi_int i);
const casadi_int* calc_hessJ_5_tags_heading_fixed_sparsity_in(casadi_int i);
const casadi_int* calc_hessJ_5_tags_heading_fixed_sparsity_out(casadi_int i);
int calc_hessJ_5_tags_heading_fixed_work(casadi_int* sz_arg, casadi_int* sz_res,
                                         casadi_int* sz_iw, casadi_int* sz_w);
int calc_hessJ_5_tags_heading_fixed_work_bytes(casadi_int* sz_arg,
                                               casadi_int* sz_res,
                                               casadi_int* sz_iw,
                                               casadi_int* sz_w);
#define calc_hessJ_5_tags_heading_fixed_SZ_ARG 12
#define calc_hessJ_5_tags_heading_fixed_SZ_RES 1
#define calc_hessJ_5_tags_heading_fixed_SZ_IW 0
#define calc_hessJ_5_tags_heading_fixed_SZ_W 764
#ifdef __cplusplus
}  // extern "C"
#endif
