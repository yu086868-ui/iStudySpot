import { Navigate, Route, Routes } from 'react-router-dom'
import AdminLayout from './layouts/AdminLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import UserList from './pages/users/UserList'
import StudyRoomList from './pages/studyrooms/StudyRoomList'
import StudyRoomDetail from './pages/studyrooms/StudyRoomDetail'
import SeatList from './pages/seats/SeatList'
import OrderList from './pages/orders/OrderList'
import AnnouncementList from './pages/announcements/AnnouncementList'
import RuleList from './pages/rules/RuleList'
import HealthCheck from './pages/system/HealthCheck'
import { getToken } from './utils/auth'

function PrivateRoute({ children }) {
  return getToken() ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <AdminLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="users" element={<UserList />} />
        <Route path="studyrooms" element={<StudyRoomList />} />
        <Route path="studyrooms/:id" element={<StudyRoomDetail />} />
        <Route path="studyrooms/:id/seats" element={<SeatList />} />
        <Route path="orders" element={<OrderList />} />
        <Route path="announcements" element={<AnnouncementList />} />
        <Route path="rules" element={<RuleList />} />
        <Route path="system/health" element={<HealthCheck />} />
      </Route>
    </Routes>
  )
}
