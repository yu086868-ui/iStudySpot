import { authApi, userApi, studyRoomApi, seatApi, reservationApi, checkInApi, announcementApi, ruleApi } from '../services';

Page({
  data: {
    userInfo: null as any,
    studyRooms: [] as any[],
    reservations: [] as any[],
    announcements: [] as any[]
  },

  onLoad() {
    this.loadInitialData();
  },

  async loadInitialData() {
    await this.loadUserInfo();
    await this.loadStudyRooms();
    await this.loadAnnouncements();
  },

  async loadUserInfo() {
    try {
      const response = await userApi.getCurrentUser();
      if (response.code === 200) {
        this.setData({ userInfo: response.data });
      }
    } catch (error) {
      console.error('加载用户信息失败', error);
    }
  },

  async loadStudyRooms() {
    try {
      const response = await studyRoomApi.getStudyRooms({ status: 'open', page: 1, pageSize: 10 });
      if (response.code === 200) {
        this.setData({ studyRooms: response.data.list });
      }
    } catch (error) {
      console.error('加载自习室列表失败', error);
    }
  },

  async loadAnnouncements() {
    try {
      const response = await announcementApi.getAnnouncements({ priority: 'high', page: 1, pageSize: 5 });
      if (response.code === 200) {
        this.setData({ announcements: response.data.list });
      }
    } catch (error) {
      console.error('加载公告列表失败', error);
    }
  },

  async handleLogin() {
    try {
      const response = await authApi.login({
        username: 'user001',
        password: 'password123'
      });

      if (response.code === 200) {
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });
        await this.loadUserInfo();
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('登录失败', error);
    }
  },

  async handleLogout() {
    try {
      const response = await authApi.logout();
      if (response.code === 200) {
        wx.showToast({
          title: '登出成功',
          icon: 'success'
        });
        this.setData({ userInfo: null });
      }
    } catch (error) {
      console.error('登出失败', error);
    }
  },

  async handleCreateReservation() {
    try {
      const response = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_room_001_1_1',
        startTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() + 27 * 60 * 60 * 1000).toISOString()
      });

      if (response.code === 200) {
        wx.showToast({
          title: '预约成功',
          icon: 'success'
        });
        await this.loadMyReservations();
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('预约失败', error);
    }
  },

  async loadMyReservations() {
    try {
      const response = await reservationApi.getMyReservations({ status: 'confirmed', page: 1, pageSize: 10 });
      if (response.code === 200) {
        this.setData({ reservations: response.data.list });
      }
    } catch (error) {
      console.error('加载预约列表失败', error);
    }
  },

  async handleCheckIn() {
    try {
      const response = await checkInApi.checkIn({
        reservationId: 'res_001',
        seatId: 'seat_room_001_1_1'
      });

      if (response.code === 200) {
        wx.showToast({
          title: '签到成功',
          icon: 'success'
        });
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('签到失败', error);
    }
  },

  async handleCheckOut() {
    try {
      const response = await checkInApi.checkOut({
        checkInRecordId: 'checkin_001'
      });

      if (response.code === 200) {
        wx.showToast({
          title: '签退成功',
          icon: 'success'
        });
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('签退失败', error);
    }
  },

  async handleCancelReservation(e: any) {
    const reservationId = e.currentTarget.dataset.id;
    try {
      const response = await reservationApi.cancelReservation(reservationId);
      if (response.code === 200) {
        wx.showToast({
          title: '取消成功',
          icon: 'success'
        });
        await this.loadMyReservations();
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('取消预约失败', error);
    }
  },

  async handleUpdateUserInfo() {
    try {
      const response = await userApi.updateUser({
        nickname: '新昵称',
        avatar: 'https://example.com/new-avatar.jpg'
      });

      if (response.code === 200) {
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        });
        await this.loadUserInfo();
      } else {
        wx.showToast({
          title: response.message,
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('更新用户信息失败', error);
    }
  },

  async loadSeats(e: any) {
    const studyRoomId = e.currentTarget.dataset.id;
    try {
      const response = await seatApi.getSeats(studyRoomId, { status: 'available' });
      if (response.code === 200) {
        console.log('座位列表', response.data);
      }
    } catch (error) {
      console.error('加载座位列表失败', error);
    }
  },

  async loadRules() {
    try {
      const response = await ruleApi.getRules({ category: 'booking' });
      if (response.code === 200) {
        console.log('规则列表', response.data);
      }
    } catch (error) {
      console.error('加载规则列表失败', error);
    }
  }
});
