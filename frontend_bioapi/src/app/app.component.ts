import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BioPortalService } from './services/bio-portal.service';
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
} from './models/biometrie.models';

type PortalView = 'overview' | 'registry' | 'adjudication' | 'agents' | 'geo';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly api = inject(BioPortalService);

  activeView: PortalView = 'overview';
  loading = false;
  scanningAbis = false;
  toastMessage: string | null = null;

  stats: PortalStats = {
    totalEnrollments: 0,
    terminalCount: 0,
    sqlImportCount: 0,
    withBiometricsCount: 0,
    totalFingerprints: 0,
    totalIris: 0,
    agentsCount: 0,
    paysCount: 0,
    provincesCount: 0,
    communesCount: 0,
    pendingAdjudications: 0,
    confirmedDuplicates: 0,
    clearedCases: 0
  };

  // Registre unifié
  enrollments: EnrollmentRow[] = [];
  registryOriginFilter: 'all' | 'app' | 'sql' | 'adjudication' | 'bio' = 'all';
  registrySearch = '';
  registryPage = 1;
  readonly pageSize = 15;

  // Modale dossier citoyen + résultats ABIS 1:N
  selectedUser: UtilisateurFull | null = null;
  selectedUserSlaps: { key: string; uri: string }[] = [];
  selectedUserStandards: { key: string; bytes: number }[] = [];
  oneToManyResults: AbisMatchResult[] | null = null;
  runningOneToMany = false;

  // Modale édition
  editingUser: {
    id: number;
    nom: string;
    prenom: string;
    sexe: string;
    dateNaissance: string;
    localite: string;
    emploiTenu: string;
    contact1: string;
    groupeSanguin: string;
  } | null = null;

  // Adjudication manuelle & ABIS
  adjudications: AdjudicationCase[] = [];
  adjudicationFilter: 'ALL' | 'EN_ATTENTE' | 'DOUBLON_CONFIRME' | 'DIFFERENTS_VALIDES' = 'EN_ATTENTE';
  activeWorkbench: AdjudicationDetail | null = null;
  workbenchFingerScores: { pos: string; label: string; score: number }[] = [];
  decisionOperator = '';
  decisionComment = '';
  submittingDecision = false;

  // Agents
  agents: AgentRow[] = [];
  agentSearch = '';
  selectedAgentModal: {
    entity: Record<string, unknown>;
    photoUri: string | null;
    fingers: { label: string; uri: string }[];
  } | null = null;

  // Référentiels géographiques
  paysList: Pays[] = [];
  provincesList: Province[] = [];
  communesList: Commune[] = [];
  paysMap: Record<number, string> = {};
  provinceMap: Record<number, string> = {};
  communeMap: Record<number, string> = {};
  selectedProvinceId: number | null = null;
  geoSearch = '';

  // Zoom image biométrique
  zoomedImage: { title: string; src: string } | null = null;

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.loading = true;
    this.api.getStats().subscribe({
      next: (s) => (this.stats = s),
      error: () => this.showToast('Erreur de connexion au serveur Spring Boot (:8080)')
    });

    this.api.getEnrollments().subscribe({
      next: (rows) => {
        this.enrollments = rows;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });

    this.api.getAdjudications().subscribe({
      next: (cases) => (this.adjudications = cases)
    });

    this.api.getAgents().subscribe({
      next: (ag) => {
        this.agents = ag;
        if (!this.decisionOperator && ag.length > 0) {
          const first = ag[0];
          this.decisionOperator = `${first.nom || ''} ${first.prenom || first.prenomutilisateur || ''}`.trim();
        }
      }
    });

    this.api.getGeo().subscribe({
      next: (g) => {
        this.paysList = g.pays || [];
        this.provincesList = g.provinces || [];
        this.communesList = g.communes || [];
        this.paysMap = {};
        this.paysList.forEach((p) => (this.paysMap[p.idpays] = p.libellepays));
        this.provinceMap = {};
        this.provincesList.forEach((pr) => (this.provinceMap[pr.idprovince] = pr.libelleprovince));
        this.communeMap = {};
        this.communesList.forEach((c) => (this.communeMap[c.idcommune] = c.libellecommune));
      }
    });
  }

  switchView(view: PortalView): void {
    this.activeView = view;
  }

  // ── Filtres & Pagination Registre ──
  get filteredEnrollments(): EnrollmentRow[] {
    const q = this.registrySearch.trim().toLowerCase();
    return this.enrollments.filter((u) => {
      if (this.registryOriginFilter === 'app' && u.sourceIdentificationId !== null) return false;
      if (this.registryOriginFilter === 'sql' && u.sourceIdentificationId === null) return false;
      if (this.registryOriginFilter === 'adjudication' && u.statut !== 'EN_ADJUDICATION') return false;
      if (this.registryOriginFilter === 'bio' && u.nbEmpreintes === 0 && !u.facePhotoBase64) return false;

      if (!q) return true;
      const hay = [
        u.id,
        u.uin,
        u.matricule,
        u.nom,
        u.nomJeuneFille,
        u.prenom,
        u.localite,
        u.lieuNaissance,
        u.emploiTenu,
        u.categorie,
        u.statut
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return hay.includes(q);
    });
  }

  get pagedEnrollments(): EnrollmentRow[] {
    const list = this.filteredEnrollments;
    const maxPage = Math.max(1, Math.ceil(list.length / this.pageSize));
    if (this.registryPage > maxPage) {
      this.registryPage = 1;
    }
    const start = (this.registryPage - 1) * this.pageSize;
    return list.slice(start, start + this.pageSize);
  }

  get totalRegistryPages(): number {
    return Math.max(1, Math.ceil(this.filteredEnrollments.length / this.pageSize));
  }

  setOriginFilter(f: 'all' | 'app' | 'sql' | 'adjudication' | 'bio'): void {
    this.registryOriginFilter = f;
    this.registryPage = 1;
  }

  // ── Dossier Citoyen & 1:N ABIS ──
  openUserDossier(id: number): void {
    this.selectedUser = null;
    this.selectedUserSlaps = [];
    this.selectedUserStandards = [];
    this.oneToManyResults = null;

    this.api.getEnrollmentDetail(id).subscribe({
      next: (u) => {
        this.selectedUser = u;
        if (u.sourceIdentificationId) {
          this.api.getExtraIdentification(u.sourceIdentificationId).subscribe({
            next: (extra) => {
              this.selectedUserSlaps = Object.entries(extra.slapImages || {})
                .filter(([, uri]) => uri.startsWith('data:image/'))
                .map(([key, uri]) => ({ key, uri }));
              this.selectedUserStandards = Object.entries(extra.standardPayloads || {}).map(
                ([key, bytes]) => ({ key, bytes })
              );
            }
          });
        }
      }
    });
  }

  closeUserDossier(): void {
    this.selectedUser = null;
    this.oneToManyResults = null;
  }

  runOneToManyOnSelected(): void {
    if (!this.selectedUser) return;
    this.runningOneToMany = true;
    this.api.matchOneToMany(this.selectedUser.id).subscribe({
      next: (res) => {
        this.oneToManyResults = res;
        this.runningOneToMany = false;
      },
      error: () => {
        this.runningOneToMany = false;
        this.showToast('Erreur lors de la recherche 1:N ABIS');
      }
    });
  }

  triggerSimulateDuplicate(userId: number, event?: Event): void {
    event?.stopPropagation();
    this.api.simulateDuplicate(userId).subscribe({
      next: (res) => {
        this.showToast(
          `Simulation ABIS créée (Candidat #${res.candidatId}) → ${res.casesCreated} alerte(s) envoyée(s) en Adjudication !`
        );
        this.refreshAll();
        this.activeView = 'adjudication';
        this.adjudicationFilter = 'EN_ATTENTE';
        if (res.firstCaseId) {
          this.openAdjudicationWorkbench(res.firstCaseId);
        }
      },
      error: () => this.showToast('Erreur lors de la simulation ABIS')
    });
  }

  openEditModal(row: EnrollmentRow, event: Event): void {
    event.stopPropagation();
    this.editingUser = {
      id: row.id,
      nom: row.nom || '',
      prenom: row.prenom || '',
      sexe: row.sexe || '',
      dateNaissance: row.dateNaissance || '',
      localite: row.localite || '',
      emploiTenu: row.emploiTenu || '',
      contact1: row.contact1 || '',
      groupeSanguin: row.groupeSanguin || ''
    };
  }

  saveEditUser(): void {
    if (!this.editingUser) return;
    const { id, ...payload } = this.editingUser;
    this.api.updateEnrollment(id, payload).subscribe({
      next: () => {
        this.editingUser = null;
        this.showToast('Enrôlement mis à jour avec succès');
        this.refreshAll();
      },
      error: () => this.showToast('Erreur lors de la mise à jour')
    });
  }

  confirmDeleteUser(id: number, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Confirmer la suppression définitive du dossier #${id} ?`)) return;
    this.api.deleteEnrollment(id).subscribe({
      next: () => {
        this.showToast(`Dossier #${id} supprimé`);
        this.refreshAll();
      },
      error: () => this.showToast('Erreur lors de la suppression')
    });
  }

  // ── ABIS Global Scan & Adjudication Workbench ──
  triggerGlobalAbisScan(): void {
    this.scanningAbis = true;
    this.api.scanRegistryAbis().subscribe({
      next: (res) => {
        this.scanningAbis = false;
        this.showToast(
          `Scan ABIS 1:N terminé en ${res.durationMs} ms : ${res.pairsCompared} paires analysées, ${res.newCasesDetected} nouveau(x) doublon(s) suspect(s) !`
        );
        this.refreshAll();
        this.activeView = 'adjudication';
      },
      error: () => {
        this.scanningAbis = false;
        this.showToast('Erreur lors du scan ABIS');
      }
    });
  }

  get filteredAdjudications(): AdjudicationCase[] {
    if (this.adjudicationFilter === 'ALL') return this.adjudications;
    return this.adjudications.filter((c) => c.statut === this.adjudicationFilter);
  }

  openAdjudicationWorkbench(caseId: number): void {
    this.api.getAdjudicationDetail(caseId).subscribe({
      next: (detail) => {
        this.activeWorkbench = detail;
        this.decisionComment = detail.caseInfo.commentaireDecision || '';
        this.workbenchFingerScores = [];
        try {
          const parsed = JSON.parse(detail.caseInfo.detailsComparaisonJson || '{}') as Record<
            string,
            number
          >;
          this.workbenchFingerScores = Object.entries(parsed).map(([pos, score]) => ({
            pos,
            label: this.formatFingerLabel(pos),
            score
          }));
        } catch {
          this.workbenchFingerScores = [];
        }
      }
    });
  }

  submitAdjudicationDecision(decision: 'DOUBLON_CONFIRME' | 'DIFFERENTS_VALIDES'): void {
    if (!this.activeWorkbench) return;
    this.submittingDecision = true;
    const caseId = this.activeWorkbench.caseInfo.id;
    this.api
      .decideAdjudication(caseId, decision, this.decisionOperator, this.decisionComment)
      .subscribe({
        next: () => {
          this.submittingDecision = false;
          this.showToast(
            decision === 'DOUBLON_CONFIRME'
              ? `Dossier #${caseId} classé DOUBLON CONFIRMÉ (candidat rejeté)`
              : `Dossier #${caseId} validé PERSONNES DIFFÉRENTES (UIN approuvé)`
          );
          this.activeWorkbench = null;
          this.refreshAll();
        },
        error: () => {
          this.submittingDecision = false;
          this.showToast('Erreur lors de l’enregistrement de la décision');
        }
      });
  }

  // ── Agents & Géo ──
  get filteredAgents(): AgentRow[] {
    const q = this.agentSearch.trim().toLowerCase();
    if (!q) return this.agents;
    return this.agents.filter((a) =>
      [a.nom, a.prenom, a.nomutilisateur, a.loginutilisateur, a.typeutilisateur]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(q)
    );
  }

  openAgentDetail(id: number): void {
    this.api.getAgentDetail(id).subscribe({
      next: (res) => {
        const fingers = Object.entries(res.fingerImages || {})
          .filter(([, uri]) => uri.startsWith('data:image/'))
          .map(([label, uri]) => ({ label, uri }));
        this.selectedAgentModal = {
          entity: res.entity || {},
          photoUri: res.photoUri,
          fingers
        };
      }
    });
  }

  get filteredCommunes(): Commune[] {
    const q = this.geoSearch.trim().toLowerCase();
    return this.communesList.filter((c) => {
      if (this.selectedProvinceId !== null && c.idprovince !== this.selectedProvinceId) return false;
      if (!q) return true;
      return c.libellecommune?.toLowerCase().includes(q);
    });
  }

  // ── Helpers Visuels ──
  buildImageSrc(b64: string | null | undefined): string | null {
    if (!b64 || b64.length < 20) return null;
    if (b64.startsWith('data:image/')) return b64;
    if (b64.startsWith('iVBOR')) return `data:image/png;base64,${b64}`;
    if (b64.startsWith('/9j/')) return `data:image/jpeg;base64,${b64}`;
    return `data:image/bmp;base64,${b64}`;
  }

  initials(nom: string | null | undefined, prenom: string | null | undefined): string {
    const a = (nom || '').trim().charAt(0);
    const b = (prenom || '').trim().charAt(0);
    return (a + b).toUpperCase() || 'ID';
  }

  formatFingerLabel(pos: string | null | undefined): string {
    if (!pos) return 'Doigt';
    const map: Record<string, string> = {
      thumb_1: 'Pouce Droit',
      thumb_0: 'Pouce Gauche',
      right_0: 'Index Droit',
      right_1: 'Majeur Droit',
      right_2: 'Annulaire Droit',
      right_3: 'Auriculaire Droit',
      left_3: 'Index Gauche',
      left_2: 'Majeur Gauche',
      left_1: 'Annulaire Gauche',
      left_0: 'Auriculaire Gauche'
    };
    return map[pos] || pos;
  }

  sameField(a: string | null | undefined, b: string | null | undefined): boolean {
    if (!a || !b) return false;
    return a.trim().toLowerCase() === b.trim().toLowerCase();
  }

  openZoom(title: string, src: string | null): void {
    if (!src) return;
    this.zoomedImage = { title, src };
  }

  agentAttr(key: string): string {
    if (!this.selectedAgentModal) return '—';
    const v = this.selectedAgentModal.entity[key];
    return v !== null && v !== undefined && v !== '' ? String(v) : '—';
  }

  private showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => {
      if (this.toastMessage === msg) {
        this.toastMessage = null;
      }
    }, 5500);
  }
}
