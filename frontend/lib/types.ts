/**
 * Tipos que reflejan los DTO del backend (com.lrch.javalab).
 * Si cambia un DTO, hay que actualizar este archivo.
 */

export type Pagination = {
  total: number;
  page: number;
  per_page: number;
  total_pages: number;
};

export type Envelope<T> = {
  success: boolean;
  data: T;
  message?: string;
  error?: string;
  pagination?: Pagination;
};

export type Rol = "USER" | "SUPER_ADMIN";

export type User = {
  id: number;
  email: string;
  displayName: string;
  createdAt: string;
  role: Rol;
};

/** Fila del panel de administracion. */
export type AdminUser = {
  id: number;
  email: string;
  displayName: string;
  role: Rol;
  active: boolean;
  createdAt: string;
  lastLoginAt?: number;
  /** Solo viene en el listado; los demas endpoints de admin no lo traen. */
  levels?: LevelProgress[];
};

export type LevelProgress = {
  level: number;
  total: number;
  solved: number;
  percent: number;
};

export type ProgressSummary = {
  xp: number;
  solved: number;
  total: number;
  streakDays: number;
  attempts: number;
  levels: LevelProgress[];
};

export type Level = {
  level: number;
  name: string;
  total: number;
  solved: number;
  percent: number;
  topics: string[];
  sections: TopicSection[];
};

export type TopicSection = {
  id: string;
  name: string;
  description: string;
  total: number;
  solved: number;
};

export type ExerciseSummary = {
  slug: string;
  level: number;
  topic: string;
  topicName: string;
  title: string;
  difficulty: string | null;
  estimatedMinutes: number | null;
  testsCount: number;
  solved: boolean;
  attempted: boolean;
};

export type ComplexityQuestion = {
  prompt: string;
  options: string[];
};

export type ComplexityResult = {
  answered: boolean;
  correct: boolean;
  correctIndex: number | null;
  explanation: string | null;
};

export type ExerciseDetail = ExerciseSummary & {
  topicDescription: string;
  nextSlug: string | null;
  nextTitle: string | null;
  topicTheory: string | null;
  statement: string;
  methodSignature: string;
  className: string;
  starterCode: string;
  complexityQuestion: ComplexityQuestion | null;
  hintsCount: number;
  attempts: number;
  savedCode: string | null;
};

export type Hint = {
  slug: string;
  hintIndex: number;
  hint: string;
  totalHints: number;
  hasMore: boolean;
};

export type TestCaseOutcome = {
  name: string;
  passed: boolean;
  skipped: boolean;
  message: string | null;
  durationMillis: number;
  /** Cuerpo del test: la llamada con sus argumentos. */
  input?: string | null;
  /** Lo que tu código imprimió en consola durante ese test. */
  output?: string | null;
};

export type SubmissionStatus =
  | "PASSED"
  | "FAILED"
  | "COMPILE_ERROR"
  | "TIMEOUT"
  | "INTERNAL_ERROR";

export type SubmissionResult = {
  exerciseSlug: string;
  exerciseTitle: string;
  status: SubmissionStatus;
  solved: boolean;
  compiled: boolean;
  passed: number;
  failed: number;
  total: number;
  tests: TestCaseOutcome[];
  compileOutput: string | null;
  consoleOutput: string | null;
  durationMillis: number;
  backend: string;
  yaEstabaResuelto: boolean;
  complexity: ComplexityResult | null;
};

export type Attempt = {
  exerciseSlug: string;
  exerciseTitle: string;
  level: number;
  passed: boolean;
  status: string;
  testsPassed: number;
  testsTotal: number;
  createdAt: string;
};

export type QuestionStatus = "PENDING" | "CORRECT" | "FAILED";

export type QuestionView = {
  id: string;
  topic: string;
  difficulty: string;
  question: string;
  code: string | null;
  options: string[];
  status: QuestionStatus;
};

export type QuestionAnswer = {
  correct: boolean;
  correctIndex: number;
  explanation: string;
};

export type QuestionTopic = {
  topic: string;
  total: number;
  answered: number;
  correct: number;
};
