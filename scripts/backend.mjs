import { spawn, spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const mode = process.argv[2];
const goals = { dev: ['spring-boot:run'], test: ['test'], package: ['package'] };
if (!Object.hasOwn(goals, mode)) throw new Error('Use dev, test ou package.');
if (mode === 'dev') {
  const missing = ['DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET'].filter(key => !process.env[key]);
  if (missing.length) {
    console.error(`Preencha ${missing.join(', ')} no arquivo .env da raiz (copie .env.example).`);
    process.exit(1);
  }
  if (!process.env.DB_URL.startsWith('jdbc:postgresql://') || /SEU_HOST|SEU_PROJECT_REF/.test(process.env.DB_URL + process.env.DB_USERNAME)) {
    console.error('Configure DB_URL e DB_USERNAME com a conexão PostgreSQL do seu projeto Supabase.');
    process.exit(1);
  }
  if (Buffer.byteLength(process.env.JWT_SECRET) < 32) {
    console.error('JWT_SECRET deve ter pelo menos 32 bytes. Gere com: openssl rand -hex 32');
    process.exit(1);
  }
}
const windows = process.platform === 'win32';
const systemMaven = spawnSync(windows ? 'mvn.cmd' : 'mvn', ['--version'], { stdio: 'ignore', shell: windows });
const command = systemMaven.status === 0 ? (windows ? 'mvn.cmd' : 'mvn') : (windows ? 'mvnw.cmd' : './mvnw');
const child = spawn(command, ['-B', ...goals[mode]], {
  cwd: fileURLToPath(new URL('../backend/', import.meta.url)),
  stdio: 'inherit', shell: windows,
  // DEBUG is used by some editors; Spring interprets it as verbose logging,
  // which can include request bodies and credentials.
  env: { ...process.env, DEBUG: 'false' },
});
child.on('error', error => { console.error(`Não foi possível iniciar Maven/Java 21: ${error.message}`); process.exitCode = 1; });
child.on('exit', code => { process.exitCode = code ?? 1; });
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => child.kill(signal));
