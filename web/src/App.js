import './App.css';
import { useEffect, useMemo, useState } from 'react';

function App() {
  const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
  const [screen, setScreen] = useState('landing');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [loginStatus, setLoginStatus] = useState({ loading: false, error: '' });
  const [registerForm, setRegisterForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [registerStatus, setRegisterStatus] = useState({ loading: false, error: '', success: '' });
  const [authUser, setAuthUser] = useState(null);
  const [showRegisterSuccess, setShowRegisterSuccess] = useState(false);
  const [tickets, setTickets] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [departmentLoadStatus, setDepartmentLoadStatus] = useState({ loading: false, error: '' });
  const [ticketForm, setTicketForm] = useState({
    title: '',
    department: '',
    description: ''
  });
  const [ticketLoadStatus, setTicketLoadStatus] = useState({ loading: false, error: '' });
  const [ticketStatus, setTicketStatus] = useState({ loading: false, error: '', success: '' });

  const userName = useMemo(() => authUser?.name || 'Student User', [authUser]);

  const ticketStats = useMemo(() => {
    const counts = {
      total: tickets.length,
      open: 0,
      inProgress: 0,
      resolved: 0
    };

    tickets.forEach((ticket) => {
      if (ticket.status === 'OPEN') {
        counts.open += 1;
      }
      if (ticket.status === 'IN_PROGRESS') {
        counts.inProgress += 1;
      }
      if (ticket.status === 'RESOLVED') {
        counts.resolved += 1;
      }
    });

    return counts;
  }, [tickets]);

  const formatTicketDate = (value) => new Date(value).toLocaleString([], {
    dateStyle: 'medium',
    timeStyle: 'short'
  });

  const loadTickets = async () => {
    const token = localStorage.getItem('supportstack_access_token');
    if (!token) {
      setTicketLoadStatus({ loading: false, error: 'Session expired. Please log in again.' });
      return;
    }

    setTicketLoadStatus({ loading: true, error: '' });

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/tickets`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload?.error?.message || 'Failed to load tickets.');
      }

      setTickets(payload?.data || []);
      setTicketLoadStatus({ loading: false, error: '' });
    } catch (error) {
      setTicketLoadStatus({ loading: false, error: error.message });
    }
  };

  const loadDepartments = async () => {
    const token = localStorage.getItem('supportstack_access_token');
    if (!token) {
      setDepartmentLoadStatus({ loading: false, error: 'Session expired. Please log in again.' });
      return;
    }

    setDepartmentLoadStatus({ loading: true, error: '' });

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/departments`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload?.error?.message || 'Failed to load departments.');
      }

      setDepartments(payload?.data || []);
      setDepartmentLoadStatus({ loading: false, error: '' });
    } catch (error) {
      setDepartmentLoadStatus({ loading: false, error: error.message });
    }
  };

  useEffect(() => {
    if (screen !== 'home') {
      return undefined;
    }

    void loadTickets();
    void loadDepartments();
    return undefined;
  }, [screen, authUser, apiBaseUrl]);

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
      localStorage.removeItem('supportstack_access_token');
      setAuthUser(null);
      setLoginStatus({ loading: false, error: error.message });
    }
  };

  const onLogout = () => {
    localStorage.removeItem('supportstack_access_token');
    setAuthUser(null);
    setTickets([]);
    setTicketForm({ title: '', department: '', description: '' });
    setTicketLoadStatus({ loading: false, error: '' });
    setTicketStatus({ loading: false, error: '', success: '' });
    setScreen('landing');
  };

  const onTicketSubmit = async (event) => {
    event.preventDefault();
    const token = localStorage.getItem('supportstack_access_token');
    if (!token) {
      setTicketStatus({ loading: false, error: 'Session expired. Please log in again.', success: '' });
      return;
    }

    setTicketStatus({ loading: true, error: '', success: '' });

    try {
      const response = await fetch(`${apiBaseUrl}/api/v1/tickets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(ticketForm)
      });

      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload?.error?.message || 'Ticket creation failed.');
      }

      setTicketForm({ title: '', department: '', description: '' });
      setTicketStatus({ loading: false, error: '', success: 'Ticket created successfully.' });
      await loadTickets();
    } catch (error) {
      setTicketStatus({ loading: false, error: error.message, success: '' });
    }
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
          email: registerForm.email,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword
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
            <button className="btn btn-google" type="button" disabled>
              Continue with Google (disabled for now)
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

            <button className="btn btn-primary" type="submit" disabled={registerStatus.loading}>
              {registerStatus.loading ? 'Creating...' : 'Create Account'}
            </button>

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
              <p className="subtitle">Track your requests, submit a new ticket, and review your own ticket history.</p>
            </div>
            <button className="btn btn-secondary" onClick={onLogout}>Log Out</button>
          </div>

          <div className="stats-grid">
            <article className="stat-card">
              <p>Total Tickets</p>
              <strong>{ticketStats.total}</strong>
            </article>
            <article className="stat-card">
              <p>Open</p>
              <strong>{ticketStats.open}</strong>
            </article>
            <article className="stat-card">
              <p>In Progress</p>
              <strong>{ticketStats.inProgress}</strong>
            </article>
            <article className="stat-card">
              <p>Resolved</p>
              <strong>{ticketStats.resolved}</strong>
            </article>
          </div>

          <div className="dashboard-grid">
            <section className="dashboard-panel">
              <div className="panel-header">
                <h3>Create Ticket</h3>
                <p>Send a new support request to the helpdesk.</p>
              </div>

              <form className="form ticket-form" onSubmit={onTicketSubmit}>
                <label htmlFor="ticketTitle">Title</label>
                <input
                  id="ticketTitle"
                  type="text"
                  value={ticketForm.title}
                  onChange={(event) => setTicketForm({ ...ticketForm, title: event.target.value })}
                  required
                  placeholder="e.g. Projector not working"
                />

                <label htmlFor="ticketDepartment">Department</label>
                <select
                  id="ticketDepartment"
                  value={ticketForm.department}
                  onChange={(event) => setTicketForm({ ...ticketForm, department: event.target.value })}
                  required
                >
                  <option value="">-- Select a Department --</option>
                  {departments.map((dept) => (
                    <option key={dept.id} value={dept.code}>
                      {dept.name}
                    </option>
                  ))}
                </select>
                {departmentLoadStatus.error && <p className="form-error">{departmentLoadStatus.error}</p>}

                <label htmlFor="ticketDescription">Description</label>
                <textarea
                  id="ticketDescription"
                  rows="6"
                  value={ticketForm.description}
                  onChange={(event) => setTicketForm({ ...ticketForm, description: event.target.value })}
                  required
                  placeholder="Describe the issue clearly so staff can help faster."
                />

                <button className="btn btn-primary" type="submit" disabled={ticketStatus.loading}>
                  {ticketStatus.loading ? 'Submitting...' : 'Submit Ticket'}
                </button>

                {ticketStatus.error && <p className="form-error">{ticketStatus.error}</p>}
                {ticketStatus.success && <p className="form-success">{ticketStatus.success}</p>}
              </form>
            </section>

            <section className="dashboard-panel dashboard-panel-wide">
              <div className="panel-header panel-header-row">
                <div>
                  <h3>My Tickets</h3>
                  <p>Your own requests sorted by newest first.</p>
                </div>
                <button className="btn btn-secondary" type="button" onClick={() => void loadTickets()}>
                  Refresh
                </button>
              </div>

              {ticketLoadStatus.loading && <p className="panel-message">Loading your tickets...</p>}
              {ticketLoadStatus.error && <p className="form-error">{ticketLoadStatus.error}</p>}

              {!ticketLoadStatus.loading && !ticketLoadStatus.error && tickets.length === 0 && (
                <p className="panel-message">You have not submitted any tickets yet.</p>
              )}

              <div className="ticket-list">
                {tickets.map((ticket) => (
                  <article className="ticket-card" key={ticket.id}>
                    <div className="ticket-card-header">
                      <div>
                        <h4>{ticket.title}</h4>
                        <p>{ticket.department}</p>
                      </div>
                      <span className={`ticket-badge ticket-badge-${ticket.status.toLowerCase().replaceAll('_', '-')}`}>
                        {ticket.status.replaceAll('_', ' ')}
                      </span>
                    </div>

                    <p className="ticket-description">{ticket.description}</p>

                    <div className="ticket-meta">
                      <span>Created {formatTicketDate(ticket.createdAt)}</span>
                      <span>Updated {formatTicketDate(ticket.updatedAt)}</span>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </div>
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