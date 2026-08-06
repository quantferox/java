/**
 * Lumeo — Main JavaScript
 *
 * Features:
 *  - Auto-dismiss flash alerts
 *  - Confirm dialogs for destructive forms (data-confirm)
 *  - Slug auto-generation from name input
 *  - Live search debounce
 *  - Active nav link highlighting
 *  - Admin sidebar mobile toggle
 */

'use strict';

document.addEventListener('DOMContentLoaded', () => {

  // ── Auto-dismiss flash alerts after 4 s ─────────────────────────────
  document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => {
      el.style.transition = 'opacity .6s ease';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 650);
    }, 4000);
  });

  // ── data-confirm attribute on <form> for custom confirm dialogs ──────
  document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', e => {
      const msg = form.dataset.confirm || 'Are you sure?';
      if (!window.confirm(msg)) e.preventDefault();
    });
  });

  // ── Slug auto-generation ─────────────────────────────────────────────
  // When a #name field exists next to a #slug field, auto-fill slug.
  const nameInput = document.querySelector('#name');
  const slugInput = document.querySelector('#slug');

  if (nameInput && slugInput) {
    let slugTouched = slugInput.value.trim() !== '';

    nameInput.addEventListener('input', () => {
      if (!slugTouched) {
        slugInput.value = toSlug(nameInput.value);
      }
    });

    slugInput.addEventListener('input', () => {
      slugTouched = slugInput.value.trim() !== '';
    });
  }

  function toSlug(text) {
    return text
      .toLowerCase()
      .trim()
      .replace(/[^\w\s-]/g, '')
      .replace(/[\s_]+/g, '-')
      .replace(/--+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

  // ── Debounced live search ────────────────────────────────────────────
  const searchInput = document.querySelector('.search-input');
  if (searchInput) {
    let timer;
    searchInput.addEventListener('input', () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        // Only auto-submit if user typed at least 3 chars or cleared the field
        if (searchInput.value.length >= 3 || searchInput.value.length === 0) {
          searchInput.closest('form')?.submit();
        }
      }, 600);
    });
  }

  // ── Active nav-link highlight (exact or prefix match) ───────────────
  const currentPath = window.location.pathname;
  document.querySelectorAll('.admin-nav .nav-item').forEach(link => {
    const href = link.getAttribute('href');
    if (href && href !== '/' && currentPath.startsWith(href)) {
      link.classList.add('active');
    }
  });

  // ── Mobile sidebar toggle ────────────────────────────────────────────
  const toggle = document.querySelector('#sidebar-toggle');
  const sidebar = document.querySelector('.admin-sidebar');
  if (toggle && sidebar) {
    toggle.addEventListener('click', () => {
      sidebar.classList.toggle('open');
    });
  }

  // ── Stock delta input guard (prevent form submit with 0) ─────────────
  document.querySelectorAll('input[name="delta"]').forEach(input => {
    input.closest('form')?.addEventListener('submit', e => {
      if (parseInt(input.value, 10) === 0) {
        e.preventDefault();
        alert('Delta must be non-zero.');
      }
    });
  });

  // ── Image preview from URL input ─────────────────────────────────────
  const imgUrlInput = document.querySelector('#imageUrl');
  if (imgUrlInput) {
    const preview = document.createElement('img');
    preview.style.cssText = 'margin-top:.5rem;max-height:120px;border-radius:6px;display:none;';
    imgUrlInput.parentNode.appendChild(preview);

    const updatePreview = () => {
      const val = imgUrlInput.value.trim();
      if (val) { preview.src = val; preview.style.display = 'block'; }
      else { preview.style.display = 'none'; }
    };

    imgUrlInput.addEventListener('input', updatePreview);
    updatePreview(); // show on page load if pre-filled
  }

  console.info('[Lumeo] JS loaded ✓');
});
