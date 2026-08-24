<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-info">
          <div class="footer-logo">
            <span class="cat-icon">🐱</span>
            <span>校园猫咪养护系统</span>
            <span class="cat-icon">🐾</span>
          </div>
          <p>为流浪猫提供一个温暖的家</p>
          <div class="cat-paw-prints">
            <span>🐾</span> <span>🐾</span> <span>🐾</span> <span>🐾</span> <span>🐾</span>
          </div>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h4>快速链接</h4>
            <router-link to="/cats">猫咪图鉴</router-link>
            <router-link to="/adoption-process">宠物领养</router-link>
            <router-link to="/tasks-square">任务广场</router-link>
            <router-link to="/donations">募捐活动</router-link>
            <router-link to="/community">社区动态</router-link>
          </div>
          <div class="link-group">
            <h4>帮助中心</h4>
            <router-link to="/about">联系我们</router-link>
          </div>
        </div>
      </div>
      <div class="copyright">
        <span class="cat-paw">🐾</span> © 2026 校园猫咪养护系统 | 用心关爱每一个生命 <span class="cat-paw">🐾</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: linear-gradient(135deg, #fff9ef 0%, #fef5e6 100%);
  color: #6B4C2C;
  padding: 3rem 0 1rem;
  margin-top: 0rem;
  border-radius: 32px 32px 0 0;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}
.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 3rem;
  margin-bottom: 2rem;
}
.footer-logo {
  font-size: 1.5rem;
  font-weight: 700;
  color: #FF9F4A;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-icon {
  font-size: 1.8rem;
  display: inline-block;
  /* animation: wave 1s ease-in-out infinite; */
  transform-origin: center;
}
@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(10deg); }
}
.footer-info p {
  color: #8B5A2B;
  font-size: 0.9rem;
  margin: 0;
}
.cat-paw-prints {
  margin-top: 12px;
  font-size: 1.2rem;
  letter-spacing: 4px;
  opacity: 0.6;
  display: flex;
  gap: 4px;
}
.cat-paw-prints span {
  animation: bounce 1s ease-in-out infinite;
  display: inline-block;
}
.cat-paw-prints span:nth-child(1) { animation-delay: 0s; }
.cat-paw-prints span:nth-child(2) { animation-delay: 0.2s; }
.cat-paw-prints span:nth-child(3) { animation-delay: 0.4s; }
.cat-paw-prints span:nth-child(4) { animation-delay: 0.6s; }
.cat-paw-prints span:nth-child(5) { animation-delay: 0.8s; }

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
.footer-links {
  display: flex;
  gap: 4rem;
}
.link-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.link-group h4 {
  color: #B45F2B;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}
.link-group a {
  color: #8B5A2B;
  text-decoration: none;
  transition: all 0.2s;
  font-size: 0.9rem;
}
.link-group a:hover {
  color: #FF9F4A;
  transform: translateX(4px);
}
.copyright {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 2px dashed #FFE0B5;
  font-size: 0.85rem;
  color: #8B5A2B;
}
.cat-paw {
  display: inline-block;
  margin: 0 4px;
  animation: bounce 0.8s ease-in-out infinite;
}
/* 响应式 */
@media (max-width: 768px) {
  .footer {
    border-radius: 24px 24px 0 0;
  }
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 2rem;
  }
  .footer-links {
    flex-direction: column;
    gap: 1.5rem;
    align-items: center;
  }
  .link-group {
    align-items: center;
  }
  .link-group a:hover {
    transform: translateX(0);
  }
  .cat-paw-prints {
    justify-content: center;
  }
}
</style>