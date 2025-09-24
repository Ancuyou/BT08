/* src/main/resources/static/js/category.js
   Replace entire file with this content.
   Requirements:
   - jQuery must be loaded before this file.
   - bootstrap.bundle.min.js (Bootstrap JS) must be loaded before this file.
*/

let categories = [];
let currentId = null;

// Bootstrap modal instances
let categoryModalEl, categoryModal;
let deleteModalEl, deleteModal;

$(document).ready(function () {
    // --- Ensure dependencies loaded ---
    if (typeof $ === 'undefined') {
        console.error('jQuery chưa được nạp. Hãy nạp jQuery trước category.js');
    }
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap JS chưa được nạp. Hãy nạp bootstrap.bundle.min.js trước category.js');
    }

    // --- modal elements & instances ---
    categoryModalEl = document.getElementById('categoryModal');
    deleteModalEl = document.getElementById('deleteModal');

    if (categoryModalEl && typeof bootstrap !== 'undefined') {
        categoryModal = new bootstrap.Modal(categoryModalEl, { backdrop: 'static' });
        // Reset form on modal hidden
        categoryModalEl.addEventListener('hidden.bs.modal', function () {
            const form = $('#categoryForm')[0];
            if (form) form.reset();
            $('#iconPreview').hide().attr('src', '');
            $('#categoryId').val('');
            $('#alert-container').empty();
        });
    }

    if (deleteModalEl && typeof bootstrap !== 'undefined') {
        deleteModal = new bootstrap.Modal(deleteModalEl);
        deleteModalEl.addEventListener('hidden.bs.modal', function () {
            currentId = null;
        });
    }

    // Event bindings
    $("#categoryForm").on('submit', function (e) {
        e.preventDefault();
        saveCategory();
    });

    $("#icon").on('change', function () {
        previewImage(this);
    });

    $("#confirmDeleteBtn").on('click', function () {
        deleteCategory(currentId);
    });

    // Delegate click handlers for dynamic rows
    $(document).on('click', '.btn-edit', function () {
        const id = $(this).data('id');
        openEditModal(id);
    });

    $(document).on('click', '.btn-delete', function () {
        const id = $(this).data('id');
        const name = $(this).data('name');
        openDeleteModal(id, name);
    });

    // Initial load
    loadCategories();
});

/* =====================
   Helpers
===================== */
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text).replace(/[&<>"']/g, function (m) {
        return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[m];
    });
}
function escapeAttr(text) {
    return escapeHtml(text).replace(/"/g, '&quot;');
}

/* =====================
   Load Categories
===================== */
function loadCategories() {
    $.get("/api/category")
        .done(function (res) {
            console.log("Response từ API:", res);
            // Accept multiple shapes: {status: true, body: [...]}, or {success:true, data:[...]} etc.
            if (res && (res.status === true || res.success === true || Array.isArray(res))) {
                categories = res.body || res.data || res;
                renderTable(categories);
            } else if (res && Array.isArray(res.body)) {
                categories = res.body;
                renderTable(categories);
            } else {
                $("#alert-container").html(
                    `<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Không có dữ liệu')}</div>`
                );
                renderTable([]);
            }
        })
        .fail(function (err) {
            console.error("Lỗi khi gọi API:", err);
            $("#alert-container").html(
                `<div class="alert alert-danger">Không thể load dữ liệu</div>`
            );
            renderTable([]);
        });
}

/* =====================
   Render Table
===================== */
function renderTable(data) {
    const tbody = $("#categoryTableBody");
    tbody.empty();

    if (!Array.isArray(data) || data.length === 0) {
        $("#emptyState").show();
        return;
    }
    $("#emptyState").hide();

    data.forEach((c) => {
        const id = escapeHtml(c.categoryId);
        const name = escapeHtml(c.categoryName);
        const nameAttr = escapeAttr(c.categoryName);
        const iconHtml = c.icon ? `<img src="/uploads/${escapeAttr(c.icon)}" style="max-height:40px;">` : "";
        const row = `
            <tr>
                <td>${id}</td>
                <td>${name}</td>
                <td>${iconHtml}</td>
                <td>
                    <button type="button" class="btn btn-warning btn-sm btn-edit" data-id="${id}">Edit</button>
                    <button type="button" class="btn btn-danger btn-sm btn-delete" data-id="${id}" data-name="${nameAttr}">Delete</button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

/* =====================
   Search Categories
===================== */
function searchCategories() {
    const term = $("#searchInput").val()?.toLowerCase() || "";
    const filtered = categories.filter((c) =>
        (c.categoryName || "").toLowerCase().includes(term)
    );
    renderTable(filtered);
}

/* =====================
   Add / Edit Modal
===================== */
function openAddModal() {
    $("#categoryId").val("");
    $("#categoryName").val("");
    $("#icon").val("");
    $("#iconPreview").hide();
    $("#categoryModalTitle").text("Add Category");

    if (categoryModal) {
        categoryModal.show();
    } else {
        // fallback (not recommended)
        $("#categoryModal").addClass('show').css('display', 'block');
    }
}

function openEditModal(id) {
    const cat = categories.find((c) => String(c.categoryId) === String(id));
    if (!cat) return;

    $("#categoryId").val(cat.categoryId);
    $("#categoryName").val(cat.categoryName);
    if (cat.icon) {
        $("#iconPreview").attr("src", "/uploads/" + cat.icon).show();
    } else {
        $("#iconPreview").hide();
    }
    $("#categoryModalTitle").text("Edit Category");

    if (categoryModal) {
        categoryModal.show();
    } else {
        $("#categoryModal").addClass('show').css('display', 'block');
    }
}

/* =====================
   Save Category
===================== */
function saveCategory() {
    const form = $("#categoryForm")[0];
    if (!form) return;
    const formData = new FormData(form);
    const id = $("#categoryId").val();
    const url = id ? "/api/category/updateCategory" : "/api/category/addCategory";

    // NOTE: many Spring controllers use POST for update; change method to 'PUT' if your backend expects PUT
    const method = 'POST';

    $.ajax({
        url: url,
        type: method,
        data: formData,
        processData: false,
        contentType: false,
        success: function (res) {
            if (res && (res.status === true || res.success === true)) {
                if (categoryModal) categoryModal.hide();
                loadCategories();
                $("#alert-container").html(
                    `<div class="alert alert-success">${escapeHtml(res.message || 'Lưu thành công')}</div>`
                );
            } else {
                $("#alert-container").html(
                    `<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Lưu thất bại')}</div>`
                );
            }
        },
        error: function (err) {
            console.error("Lỗi khi lưu Category:", err);
            $("#alert-container").html(
                `<div class="alert alert-danger">Không thể lưu Category</div>`
            );
        },
    });
}

/* =====================
   Delete Modal & action
===================== */
function openDeleteModal(id, name) {
    currentId = id;
    $("#deleteName").text(name);

    if (deleteModal) {
        deleteModal.show();
    } else {
        $("#deleteModal").addClass('show').css('display','block');
    }
}

function deleteCategory(id) {
    if (!id) return;
    $.ajax({
        url: "/api/category/deleteCategory?categoryId=" + encodeURIComponent(id),
        type: "DELETE",
        success: function (res) {
            if (res && (res.status === true || res.success === true)) {
                if (deleteModal) deleteModal.hide();
                loadCategories();
                $("#alert-container").html(
                    `<div class="alert alert-success">${escapeHtml(res.message || 'Xóa thành công')}</div>`
                );
            } else {
                $("#alert-container").html(
                    `<div class="alert alert-danger">${escapeHtml(res && res.message ? res.message : 'Xóa thất bại')}</div>`
                );
            }
        },
        error: function (err) {
            console.error("Lỗi khi xóa Category:", err);
            $("#alert-container").html(
                `<div class="alert alert-danger">Không thể xóa Category</div>`
            );
        },
    });
}

/* =====================
   Preview Image
===================== */
function previewImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            $("#iconPreview").attr("src", e.target.result).show();
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        $("#iconPreview").hide().attr('src', '');
    }
}
