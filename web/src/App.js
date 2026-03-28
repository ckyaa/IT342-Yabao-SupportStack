import './App.css';
import { useMemo, useState } from 'react';

function App() {
  const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
  const [screen, setScreen] = useState('landing');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [loginStatus, setLoginStatus] = useState({ loading: false, error: '' });
  const [registerForm, setRegisterForm] = useState({
    username: '',
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [registerStatus, setRegisterStatus] = useState({ loading: false, error: '', success: '' });
  const [authUser, setAuthUser] = useState(null);
  const [showRegisterSuccess, setShowRegisterSuccess] = useState(false);

  const userName = useMemo(() => {
    if (authUser?.name) {
      return authUser.name;
    }
    if (registerForm.name.trim()) {
      return registerForm.name.trim();
    }
    return 'Student User';
  }, [authUser, registerForm.name]);

  const onLoginSubmit = async (event) => {
    event.preventDefault();
    setLoginStatus({ loading: true, error: '' });

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: loginForm.username,
          password: loginForm.password
        })
      });

      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload?.error?.message || 'Login failed.');
      }

      const token = payload?.data?.accessToken;
      const user = payload?.data?.user;
      if (token) {
        localStorage.setItem('supportstack_access_token', token);
      }
      if (user) {
        setAuthUser(user);
      }
      setLoginStatus({ loading: false, error: '' });
      setScreen('home');
    } catch (error) {
      setLoginStatus({ loading: false, error: error.message });
    }
  };

  const onGoogleSignIn = () => {
    setLoginStatus({
      loading: false,
      error: 'Google Sign-In is not yet connected on the backend. Use username and password for now.'
    });
  };

  const onLogout = () => {
    localStorage.removeItem('supportstack_access_token');
    setAuthUser(null);
    setScreen('landing');
  };

  const onRegisterSubmit = async (event) => {
    event.preventDefault();
    if (registerForm.password !== registerForm.confirmPassword) {
      setRegisterStatus({ loading: false, error: 'Passwords do not match.', success: '' });
      return;
    }
    setRegisterStatus({ loading: true, error: '', success: '' });
    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: registerForm.username,
          name: registerForm.name,
          email: registerForm.email,
          password: registerForm.password
        })
      });
      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload?.error?.message || 'Registration failed.');
      }
      if (payload?.data) {
        setAuthUser({
          username: payload.data.username,
          name: payload.data.name,
          email: payload.data.email
        });
      }
      setRegisterStatus({ loading: false, error: '', success: 'Account created successfully.' });
      setShowRegisterSuccess(true);
    } catch (error) {
      setRegisterStatus({ loading: false, error: error.message, success: '' });
    }
  };

  return (
    <main className="app-shell">
      <section className="bg-orb bg-orb-left" aria-hidden="true" />
      <section className="bg-orb bg-orb-right" aria-hidden="true" />

      {screen === 'landing' && (
        <section className="page card card-hero fade-in">
          <p className="eyebrow">School Helpdesk Platform</p>
          <h1>SupportStack</h1>
          <p className="subtitle">
            Submit, track, and resolve school support requests in one organized workflow for students, staff, and admins.
          </p>

          <div className="hero-grid">
            <article className="feature-tile">
              <h3>Student Ticket Tracking</h3>
              <p>Create requests and follow every status update from open to resolution.</p>
            </article>
            <article className="feature-tile">
              <h3>Staff Queue Management</h3>
              <p>Assign, triage, and resolve issues with clear accountability and visibility.</p>
            </article>
            <article className="feature-tile">
              <h3>Admin Oversight</h3>
              <p>Manage users, roles, trash recovery, and system-wide support operations.</p>
            </article>
          </div>

          <div className="actions">
            <button className="btn btn-primary" onClick={() => setScreen('login')}>Login</button>
            <button className="btn btn-secondary" onClick={() => setScreen('register')}>Create Account</button>
          </div>
        </section>
      )}

      {screen === 'login' && (
        <section className="page card fade-in-up">
          <button className="back-link" onClick={() => setScreen('landing')}>Back to Landing</button>
          <h2>Welcome Back</h2>
          <p className="subtitle">Log in to view your dashboard and ticket updates.</p>

          <form className="form" onSubmit={onLoginSubmit}>
            <label htmlFor="loginUsername">Username</label>
            <input
              id="loginUsername"
              type="text"
              value={loginForm.username}
              onChange={(event) => setLoginForm({ ...loginForm, username: event.target.value })}
              required
              placeholder="Enter your username"
            />

            <label htmlFor="loginPassword">Password</label>
            <input
              id="loginPassword"
              type="password"
              value={loginForm.password}
              onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
              required
              placeholder="Enter your password"
            />

            <button className="btn btn-primary" type="submit" disabled={loginStatus.loading}>
              {loginStatus.loading ? 'Signing In...' : 'Sign In'}
            </button>
            <button className="btn btn-google" type="button" onClick={onGoogleSignIn}>
              Continue with Google
            </button>

            {loginStatus.error && <p className="form-error">{loginStatus.error}</p>}
          </form>

          <p className="inline-cta">
            New here? <button className="text-btn" onClick={() => setScreen('register')}>Create an account</button>
          </p>
        </section>
      )}

      {screen === 'register' && (
        <section className="page card fade-in-up">
          <button className="back-link" onClick={() => setScreen('landing')}>Back to Landing</button>
          <h2>Create Account</h2>
          <p className="subtitle">Start your SupportStack profile in less than a minute.</p>

          <form className="form" onSubmit={onRegisterSubmit}>
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={registerForm.username}
              onChange={(event) => setRegisterForm({ ...registerForm, username: event.target.value })}
              required
              placeholder="student_username"
            />

            <label htmlFor="name">Name</label>
            <input
              id="name"
              type="text"
              value={registerForm.name}
              onChange={(event) => setRegisterForm({ ...registerForm, name: event.target.value })}
              required
              placeholder="Juan Dela Cruz"
            />

            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={registerForm.email}
              onChange={(event) => setRegisterForm({ ...registerForm, email: event.target.value })}
              required
              placeholder="student@school.edu"
            />

            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={registerForm.password}
              onChange={(event) => setRegisterForm({ ...registerForm, password: event.target.value })}
              required
              minLength={8}
              placeholder="At least 8 characters"
            />

            <label htmlFor="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              value={registerForm.confirmPassword}
              onChange={(event) => setRegisterForm({ ...registerForm, confirmPassword: event.target.value })}
              required
              minLength={8}
              placeholder="Re-enter password"
            />

            <button className="btn btn-primary" type="submit">Create Account</button>

            {registerStatus.error && <p className="form-error">{registerStatus.error}</p>}
            {registerStatus.success && <p className="form-success">{registerStatus.success}</p>}
          </form>

          <p className="inline-cta">
            Already have an account? <button className="text-btn" onClick={() => setScreen('login')}>Login</button>
          </p>
        </section>
      )}

      {screen === 'home' && (
        <section className="page card card-home fade-in-up">
          <div className="home-header">
            <div>
              <p className="eyebrow">Dashboard</p>
              <h2>Hello, {userName}</h2>
            </div>
            <button className="btn btn-secondary" onClick={onLogout}>Log Out</button>
          </div>

          <div className="stats-grid">
            <article className="stat-card">
              <p>Open Tickets</p>
              <strong>4</strong>
            </article>
            <article className="stat-card">
              <p>In Progress</p>
              <strong>2</strong>
            </article>
            <article className="stat-card">
              <p>Resolved</p>
              <strong>11</strong>
            </article>
          </div>

          <section className="ticket-preview">
            <h3>Recent Tickets</h3>
            <ul>
              <li>#SS-1024 Network outage in Computer Lab A</li>
              <li>#SS-1019 Student portal access issue</li>
              <li>#SS-1012 Printer request for Registrar Office</li>
            </ul>
          </section>
        </section>
      )}

      {showRegisterSuccess && (
        <div className="notification success">
          <span>Successful registration</span>
          <button className="close-btn" onClick={() => { setShowRegisterSuccess(false); setScreen('home'); }}>×</button>
        </div>
      )}
    </main>
  );
}

export default App;
