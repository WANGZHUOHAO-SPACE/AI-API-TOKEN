<script setup lang="ts">
import { Clock, Iphone, Message, Service } from '@element-plus/icons-vue'

const email = '19834339457@163.com'
const phone = '19834339457'
</script>

<template>
  <div class="contact-page">
    <section class="support-banner">
      <div class="banner-icon">
        <span class="pulse-ring"></span>
        <el-icon><Service /></el-icon>
      </div>
      <div>
        <strong>需要帮助？</strong>
        <p>我们的客服团队随时准备为您提供专业的支持服务</p>
      </div>
      <div class="banner-decoration"></div>
    </section>

    <section class="contact-heading">
      <span>联系我们</span>
      <h1>客户支持</h1>
      <p>我们随时为您提供帮助，请选择以下任一方式与我们联系。</p>
    </section>

    <section class="contact-grid">
      <article class="contact-card mail-card">
        <div class="card-icon"><el-icon><Message /></el-icon></div>
        <div class="card-copy">
          <h2>邮件支持</h2>
          <a :href="`mailto:${email}`">{{ email }}</a>
          <p>发送您的问题，我们会尽快回复</p>
        </div>
        <a class="contact-button" :href="`mailto:${email}`">发送邮件</a>
      </article>

      <article class="contact-card phone-card">
        <div class="card-icon"><el-icon><Iphone /></el-icon></div>
        <div class="card-copy">
          <h2>电话支持</h2>
          <a :href="`tel:${phone}`">{{ phone }}</a>
          <p>工作时间内可直接致电咨询</p>
        </div>
        <a class="contact-button" :href="`tel:${phone}`">联系我们</a>
      </article>

      <article class="contact-card time-card">
        <div class="card-icon"><el-icon><Clock /></el-icon></div>
        <div class="card-copy">
          <h2>在线时间</h2>
          <strong>周一至周日</strong>
          <p>10:00 - 21:00</p>
        </div>
        <div class="online-status"><span></span>当前服务时间</div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.contact-page {
  position: relative;
  min-height: calc(100vh - 134px);
  overflow: hidden;
  padding: 18px 0 44px;
}
.contact-page::before,
.contact-page::after {
  position: absolute;
  z-index: -1;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  content: '';
  filter: blur(16px);
  opacity: .45;
}
.contact-page::before { top: 80px; right: -130px; background: radial-gradient(circle, #dfe8ff 0, rgba(223,232,255,0) 70%); }
.contact-page::after { bottom: -180px; left: -120px; background: radial-gradient(circle, #dff8f4 0, rgba(223,248,244,0) 70%); }
.support-banner {
  position: relative;
  display: flex;
  max-width: 1180px;
  min-height: 78px;
  align-items: center;
  gap: 18px;
  margin: 0 auto 38px;
  overflow: hidden;
  padding: 16px 22px;
  border: 1px solid #dcecff;
  border-radius: 15px;
  background: linear-gradient(105deg, #eef8ff, #edf3ff 65%, #f5f0ff);
}
.banner-icon { position: relative; display: grid; width: 46px; height: 46px; flex: 0 0 46px; place-items: center; border-radius: 50%; background: #fff; color: #4095ff; font-size: 24px; box-shadow: 0 8px 22px rgba(64,149,255,.18); }
.pulse-ring { position: absolute; inset: -6px; border: 2px solid rgba(64,149,255,.22); border-radius: 50%; animation: pulse 2.2s ease-out infinite; }
.support-banner strong { color: #24324a; font-size: 16px; }
.support-banner p { margin: 5px 0 0; color: #6f7d94; font-size: 13px; }
.banner-decoration { position: absolute; top: -58px; right: 45px; width: 180px; height: 180px; border: 1px solid rgba(91,103,241,.12); border-radius: 50%; }
.contact-heading { max-width: 1180px; margin: 0 auto 34px; }
.contact-heading span { color: #3d8fff; font-size: 15px; font-weight: 700; }
.contact-heading h1 { margin: 10px 0 0; color: #172033; font-size: 34px; letter-spacing: -1px; }
.contact-heading p { margin: 17px 0 0; color: #707b90; font-size: 16px; }
.contact-grid { display: grid; max-width: 1180px; margin: 0 auto; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.contact-card { position: relative; display: flex; min-height: 290px; flex-direction: column; justify-content: space-between; overflow: hidden; padding: 30px; border: 1px solid #edf0f6; border-radius: 18px; background: rgba(255,255,255,.92); box-shadow: 0 14px 40px rgba(42,59,93,.07); transition: transform .25s ease, box-shadow .25s ease; }
.contact-card:hover { transform: translateY(-5px); box-shadow: 0 20px 48px rgba(42,59,93,.13); }
.contact-card::after { position: absolute; right: -45px; bottom: -55px; width: 150px; height: 150px; border-radius: 50%; content: ''; opacity: .12; }
.mail-card::after { background: #249cff; }.phone-card::after { background: #6d65f6; }.time-card::after { background: #ffb934; }
.card-icon { display: grid; width: 56px; height: 56px; place-items: center; border-radius: 14px; color: #fff; font-size: 28px; box-shadow: 0 10px 24px rgba(45,124,255,.2); }
.mail-card .card-icon { background: linear-gradient(135deg, #3bb2ff, #2589f4); }.phone-card .card-icon { background: linear-gradient(135deg, #8d8aff, #5d58ee); }.time-card .card-icon { background: linear-gradient(135deg, #ffd45d, #ffab3d); }
.card-copy h2 { margin: 25px 0 9px; color: #202a3e; font-size: 19px; }
.card-copy a, .card-copy strong { color: #338af4; font-size: 17px; font-weight: 700; text-decoration: none; }
.time-card .card-copy strong { color: #344057; }
.card-copy p { margin: 9px 0 0; color: #8992a5; font-size: 13px; line-height: 1.7; }
.contact-button { position: relative; z-index: 1; width: fit-content; padding: 10px 18px; border-radius: 9px; background: linear-gradient(135deg, #419cff, #247fe9); color: #fff; font-size: 13px; font-weight: 650; text-decoration: none; box-shadow: 0 8px 18px rgba(36,127,233,.2); }
.phone-card .contact-button { background: linear-gradient(135deg, #7771fb, #5a55df); }
.online-status { position: relative; z-index: 1; display: flex; width: fit-content; align-items: center; gap: 8px; padding: 9px 14px; border-radius: 999px; background: #effaf5; color: #2f9b6e; font-size: 13px; font-weight: 650; }
.online-status span { width: 8px; height: 8px; border-radius: 50%; background: #31b87d; box-shadow: 0 0 0 4px rgba(49,184,125,.13); }
@keyframes pulse { 0% { transform: scale(.82); opacity: .9; } 75%,100% { transform: scale(1.28); opacity: 0; } }
@media (max-width: 1050px) { .contact-grid { grid-template-columns: repeat(2, 1fr); }.support-banner, .contact-heading, .contact-grid { max-width: 820px; } }
@media (max-width: 700px) { .contact-grid { grid-template-columns: 1fr; }.contact-heading h1 { font-size: 28px; }.contact-card { min-height: 260px; }.banner-decoration { display: none; } }
</style>
