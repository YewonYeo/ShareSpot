<template>
  <v-container>
    <back-menu title="설정"></back-menu>

    <v-list>
      <v-subheader>Account Settings</v-subheader>
      <template>
        <v-list-item>
          <v-list-item-avatar color="blue lighten-3" @click="moveProfileModify()">
            <v-icon color="white"> mdi-account-outline </v-icon>
          </v-list-item-avatar>

          <v-list-item-content @click="moveProfileModify()">
            <v-list-item-title>프로필 변경</v-list-item-title>
            <v-list-item-subtitle>개인 정보 변경</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item>
          <v-list-item-avatar color="green lighten-2" @click="goRoute('/users/resetpass')">
            <v-icon color="white"> mdi-lock-outline </v-icon>
          </v-list-item-avatar>
          <v-list-item-content @click="goRoute('/users/resetpass')">
            <v-list-item-title>비밀번호 재설정</v-list-item-title>
            <v-list-item-subtitle>비밀변호 변경</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </template>
    </v-list>

    <v-list>
      <v-subheader>Notifications Settings</v-subheader>
      <template>
        <v-list-item>
          <v-list-item-avatar color="pink lighten-3">
            <v-icon color="white"> mdi-bell-outline </v-icon>
          </v-list-item-avatar>

          <v-list-item-content @click="goRoute('/settings/notice')">
            <v-list-item-title>알림 관리</v-list-item-title>
            <v-list-item-subtitle>알림 설정 </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>
      </template>
    </v-list>

    <v-list>
      <v-subheader>General</v-subheader>
      <template>
        <v-list-item>
          <v-list-item-avatar color="yellow darken-1">
            <v-icon color="white"> mdi-alert-circle-outline </v-icon>
          </v-list-item-avatar>

          <v-list-item-content @click="goRoute('/settings/service')">
            <v-list-item-title>정보</v-list-item-title>
            <v-list-item-subtitle>도움말, 이용약관</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item @click.prevent="logout()">
          <v-list-item-avatar color="#C37AFD">
            <v-icon color="white"> mdi-logout </v-icon>
          </v-list-item-avatar>

          <v-list-item-content>
            <v-list-item-title>로그아웃</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </template>
    </v-list>
  </v-container>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import BackMenu from "@/components/layout/BackMenu.vue";

const userStore = "userStore";

export default {
  name: "S07P12A505Settings",
  components: { BackMenu },
  data: () => ({
    accountitems: [
      { header: "Account Settings" },
      {
        bgc: "blue lighten-3",
        icon: "mdi-account-outline",
        color: "white",
        title: "프로필 변경",
        subtitle: "개인 정보 변경",
        route: "/settings/info",
      },
      { divider: true },
      {
        bgc: "green lighten-2",
        icon: "mdi-lock-outline",
        color: "white",
        title: "비밀번호 재설정",
        subtitle: "비밀번호 변경",
        route: "/users/resetpass",
      },
      { divider: true },
    ],
  }),

  computed: {
    ...mapState(userStore, ["isLogin", "userInfo"]),
  },

  mounted() {},

  methods: {
    ...mapMutations(userStore, ["SET_IS_LOGIN", "SET_USER_INFO"]),
    moveProfileModify() {
      this.$router.push({ name: "profileModify" });
    },
    logout() {
      this.SET_IS_LOGIN(false);
      this.SET_USER_INFO(null);

      sessionStorage.removeItem("Authorization");

      if (this.$route.path != "/") {
        this.$router.push({ name: "login" });
      }
      // console.log(this.userInfo);
    },
    goRoute(pageURL) {
      this.$router.push({ path: pageURL });
    },
  },
};
</script>

<style scoped>
/* hr {
    background-color: rgb(230, 230, 230);
    height: 1px;
    } */
/* .v-list-item__subtitle {
        font-size: 12px;
        color: #999999;
        font-weight: lighter;
    } */
.v-list-item-subtitle {
  font-size: 12px;
  color: #999999;
  font-weight: lighter;
}
</style>
