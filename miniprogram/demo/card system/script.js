/**
 * 卡片视觉效果 Demo - 交互脚本
 * 仅处理光效跟随、悬浮反馈等视觉交互
 */

const state = {
  mouseX: 0,
  mouseY: 0,
};

/**
 * 初始化卡片光效跟随
 * UR/LR 卡片 hover 时，光效跟随鼠标位置
 */
function initShineEffect() {
  const cards = document.querySelectorAll('.card-special');

  cards.forEach((card) => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const centerX = rect.width / 2;
      const centerY = rect.height / 2;

      // 计算倾斜角度
      const rotateX = ((y - centerY) / centerY) * -6;
      const rotateY = ((x - centerX) / centerX) * 6;

      card.style.transform = `translateY(-6px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;

      // 更新光效位置
      const shine = card.querySelector('.card-shine');
      if (shine) {
        const percentX = (x / rect.width) * 100;
        const percentY = (y / rect.height) * 100;
        shine.style.background = `radial-gradient(circle at ${percentX}% ${percentY}%, rgba(255,255,255,0.15) 0%, transparent 50%)`;
      }
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
      const shine = card.querySelector('.card-shine');
      if (shine) {
        shine.style.background = '';
      }
    });
  });
}

/**
 * 初始化普通卡轻微悬浮效果
 */
function initHoverEffect() {
  const normalCards = document.querySelectorAll('.card:not(.card-special)');

  normalCards.forEach((card) => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      const centerX = rect.width / 2;
      const centerY = rect.height / 2;

      const rotateX = ((y - centerY) / centerY) * -3;
      const rotateY = ((x - centerX) / centerX) * 3;

      card.style.transform = `translateY(-6px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
    });
  });
}

/**
 * 页面初始化
 */
function init() {
  initShineEffect();
  initHoverEffect();
}

document.addEventListener('DOMContentLoaded', init);
