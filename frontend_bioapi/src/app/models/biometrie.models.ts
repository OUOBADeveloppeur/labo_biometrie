export interface PortalStats {
  totalEnrollments: number;
  terminalCount: number;
  sqlImportCount: number;
  withBiometricsCount: number;
  totalFingerprints: number;
  totalIris: number;
  agentsCount: number;
  paysCount: number;
  provincesCount: number;
  communesCount: number;
  pendingAdjudications: number;
  confirmedDuplicates: number;
  clearedCases: number;
}

export interface EnrollmentRow {
  id: number;
  uin: string | null;
  matricule: string | null;
  nom: string | null;
  nomJeuneFille: string | null;
  prenom: string | null;
  sexe: string | null;
  dateNaissance: string | null;
  lieuNaissance: string | null;
  localite: string | null;
  village: string | null;
  situationMatrimoniale: string | null;
  categorie: string | null;
  emploiTenu: string | null;
  dernierDiplome: string | null;
  groupeSanguin: string | null;
  dateCnib: string | null;
  contact1: string | null;
  nomEquipement: string | null;
  statut: string | null;
  sourceIdentificationId: number | null;
  facePhotoBase64: string | null;
  dateCreation: string | null;
  nbEmpreintes: number;
  nbIris: number;
  firstDeviceModel: string | null;
  firstScoreNfiq: number | null;
}

export interface EmpreinteItem {
  id: number;
  positionDoigt: string | null;
  categorieDoigt: string | null;
  typeCapture: string | null;
  templateBase64: string | null;
  imageBase64: string | null;
  deviceModel: string | null;
  firmwareVersion: string | null;
  resolution: number | null;
  qualite: number | null;
  scoreNfiq: number | null;
  scoreQualiteSdk: number | null;
  surfaceContact: number | null;
  tempsCaptureMs: number | null;
  nombreTentatives: number | null;
  dateCapture: string | null;
}

export interface IrisItem {
  id: number;
  positionOeil: string | null;
  templateBase64: string | null;
  dateCapture: string | null;
}

export interface UtilisateurFull {
  id: number;
  uin: string | null;
  matricule: string | null;
  nom: string | null;
  nomJeuneFille: string | null;
  prenom: string | null;
  sexe: string | null;
  dateNaissance: string | null;
  lieuNaissance: string | null;
  localite: string | null;
  localiteOrigine: string | null;
  village: string | null;
  idCommune: number | null;
  idCommuneNaissance: number | null;
  idPays: number | null;
  faceToken: string | null;
  facePhotoBase64: string | null;
  situationMatrimoniale: string | null;
  nbEpouses: number | null;
  nbEnfants: number | null;
  categorie: string | null;
  promotion: string | null;
  emploiTenu: string | null;
  niveauInstruction: string | null;
  autreNiveauInstruction: string | null;
  dernierDiplome: string | null;
  groupeSanguin: string | null;
  dateCnib: string | null;
  situationPrime: number | null;
  numeroArme: string | null;
  numeroChassis: string | null;
  contact1: string | null;
  contact2: string | null;
  contact3: string | null;
  personnePrevenir1: string | null;
  contactPersonnePrevenir1: string | null;
  personnePrevenir2: string | null;
  contactPersonnePrevenir2: string | null;
  nomEquipement: string | null;
  tempsEnrolementTotalMs: number | null;
  statut: string | null;
  sourceIdentificationId: number | null;
  dateCreation: string | null;
  empreintes: EmpreinteItem[];
  iris: IrisItem[];
}

export interface MiniUser {
  id: number;
  uin: string | null;
  nom: string | null;
  prenom: string | null;
  sexe: string | null;
  dateNaissance: string | null;
  localite: string | null;
  statut: string | null;
  nomEquipement: string | null;
  facePhotoBase64: string | null;
  nbEmpreintes: number;
}

export interface AdjudicationCase {
  id: number;
  scoreAbis: number | null;
  scoreIdentite: number | null;
  modalite: string;
  doigtCandidat: string | null;
  doigtExistant: string | null;
  raisonAlerte: string;
  detailsComparaisonJson: string;
  statut: 'EN_ATTENTE' | 'DOUBLON_CONFIRME' | 'DIFFERENTS_VALIDES' | string;
  decisionPar: string | null;
  commentaireDecision: string | null;
  dateDetection: string | null;
  dateDecision: string | null;
  candidat: MiniUser;
  existant: MiniUser;
}

export interface AdjudicationDetail {
  caseInfo: AdjudicationCase;
  candidat: UtilisateurFull;
  existant: UtilisateurFull;
}

export interface AbisMatchResult {
  targetUserId: number;
  targetNom: string | null;
  targetPrenom: string | null;
  targetUin: string | null;
  scoreAbis: number;
  scoreIdentite: number;
  doigtCandidat: string | null;
  doigtExistant: string | null;
  modalite: string;
  raison: string;
  fingerScores: Record<string, number>;
}

export interface AgentRow {
  id: number;
  idutilisateur: number;
  nom: string | null;
  prenom: string | null;
  prenomutilisateur: string | null;
  nomutilisateur: string | null;
  loginutilisateur: string | null;
  typeutilisateur: string | null;
  genreutilisateur: string | null;
  datenaissanceutilisateur: string | null;
  lieunaissanceutilisateur: string | null;
  situationmatrimonialeutilisateur: string | null;
  contact1utilisateur: string | null;
  contact2utilisateur: string | null;
  isblocked: boolean | null;
  blocageutilisateur: boolean | null;
  raisonblocageutilisateur: string | null;
  dateheureblocageutilisateur: string | null;
  nbtentativeechoue: number | null;
  changepassword: boolean | null;
  isdeleted: boolean | null;
  createdat: string | null;
}

export interface Pays {
  idpays: number;
  libellepays: string;
}

export interface Province {
  idprovince: number;
  libelleprovince: string;
}

export interface Commune {
  idcommune: number;
  libellecommune: string;
  idprovince: number;
}

