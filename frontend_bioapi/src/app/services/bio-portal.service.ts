import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AbisMatchResult,
  AdjudicationCase,
  AdjudicationDetail,
  AgentRow,
  Commune,
  EnrollmentRow,
  Pays,
  PortalStats,
  Province,
  UtilisateurFull
} from '../models/biometrie.models';

@Injectable({ providedIn: 'root' })
export class BioPortalService {
  private readonly http = inject(HttpClient);

  getStats(): Observable<PortalStats> {
    return this.http.get<PortalStats>('/api/portal/stats');
  }

  getEnrollments(): Observable<EnrollmentRow[]> {
    return this.http.get<EnrollmentRow[]>('/api/portal/enrollments');
  }

  getEnrollmentDetail(id: number): Observable<UtilisateurFull> {
    return this.http.get<UtilisateurFull>(`/api/enroll/${id}`);
  }

  getExtraIdentification(sourceId: number): Observable<{
    slapImages: Record<string, string>;
    standardPayloads: Record<string, number>;
  }> {
    return this.http.get<{
      slapImages: Record<string, string>;
      standardPayloads: Record<string, number>;
    }>(`/api/identifications/${sourceId}`);
  }

  updateEnrollment(id: number, payload: Record<string, string>): Observable<UtilisateurFull> {
    return this.http.put<UtilisateurFull>(`/api/enroll/${id}`, payload);
  }

  deleteEnrollment(id: number): Observable<void> {
    return this.http.delete<void>(`/api/enroll/${id}`);
  }

  getAdjudications(): Observable<AdjudicationCase[]> {
    return this.http.get<AdjudicationCase[]>('/api/portal/adjudications');
  }

  getAdjudicationDetail(id: number): Observable<AdjudicationDetail> {
    return this.http.get<AdjudicationDetail>(`/api/portal/adjudications/${id}`);
  }

  decideAdjudication(
    id: number,
    decision: 'DOUBLON_CONFIRME' | 'DIFFERENTS_VALIDES',
    operateur: string,
    commentaire: string
  ): Observable<AdjudicationCase> {
    return this.http.post<AdjudicationCase>(`/api/portal/adjudications/${id}/decide`, {
      decision,
      operateur,
      commentaire
    });
  }

  scanRegistryAbis(): Observable<{
    scannedUsers: number;
    pairsCompared: number;
    newCasesDetected: number;
    totalPendingCases: number;
    durationMs: number;
  }> {
    return this.http.post<{
      scannedUsers: number;
      pairsCompared: number;
      newCasesDetected: number;
      totalPendingCases: number;
      durationMs: number;
    }>('/api/portal/abis/scan', {});
  }

  matchOneToMany(userId: number): Observable<AbisMatchResult[]> {
    return this.http.post<AbisMatchResult[]>(`/api/portal/abis/match/${userId}`, {});
  }

  simulateDuplicate(userId: number): Observable<{
    candidatId: number;
    casesCreated: number;
    firstCaseId?: number;
  }> {
    return this.http.post<{
      candidatId: number;
      casesCreated: number;
      firstCaseId?: number;
    }>(`/api/portal/abis/simulate-duplicate/${userId}`, {});
  }

  getAgents(): Observable<AgentRow[]> {
    return this.http.get<AgentRow[]>('/api/portal/agents');
  }

  getAgentDetail(id: number): Observable<{
    entity: Record<string, unknown>;
    photoUri: string | null;
    fingerImages: Record<string, string>;
  }> {
    return this.http.get<{
      entity: Record<string, unknown>;
      photoUri: string | null;
      fingerImages: Record<string, string>;
    }>(`/api/agents/${id}`);
  }

  getGeo(): Observable<{
    pays: Pays[];
    provinces: Province[];
    communes: Commune[];
  }> {
    return this.http.get<{
      pays: Pays[];
      provinces: Province[];
      communes: Commune[];
    }>('/api/portal/geo');
  }
}

