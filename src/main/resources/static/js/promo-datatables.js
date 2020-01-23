$(document).ready(function () {
    moment.locale('pt-br');

    var table = $('#table-server').DataTable({
        processing: true,
        serverSide: true,
        responsive: true,
        lengthMenu: [10, 15, 20, 25],
        ajax: {
            url: '/promocao/datatables/server',
            data: 'data'
        },
        columns: [
            {data: 'id'},
            {data: 'titulo'},
            {data: 'site'},
            {data: 'linkPromocao'},
            {data: 'descricao'},
            {data: 'linkImagem'},
            {data: 'preco', render: $.fn.dataTable.render.number('.', ',', 2, 'R$')},
            {data: 'likes'},
            {
                data: 'dtCadastro', render:
                    function (dtCadastro) {
                        return moment(dtCadastro).format('LLL');
                    }
            },
            {data: 'categoria.titulo'},

        ],

        dom: 'Bfrtip',
        buttons: [
            {
                text: 'Edit',
                attr: {
                    id: 'btn-edit',
                    type: 'button'
                },
                enabled: false
            },
            {
                text: 'Delete',
                attr: {
                    id: 'btn-delete',
                    type: 'button'
                },
                enabled: false
            }
        ]
    });

    /* Ação para marcar/desmarcar botoes ao clicar na ordenação */
    $('#table-server thead').on('click', 'tr', function () {
        $(this).removeClass();
        // desabilitar os botoes
        table.buttons().disable();
    });

    /* Ação para marcar/desmarcar linhas clicadas */
    //  so vai trabalhar em click em uma TR dentro da TBODY
    $('#table-server tbody').on('click', 'tr', function () {
        if ($(this).hasClass('selected')) {
            $(this).removeClass();
            // desabilitar os botoes
            table.buttons().disable();
            Boston, Massachusetts, EUA
        } else {
            $('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            // habilitar os botoes
            table.buttons().enable();
        }
    });

    // acao de edit
    $('#btn-edit').on('click', function () {
        if (isSelectedRow()) {
            // 1) popular os dados do modal
            // 1.1 - Pegar o id
            var promoId = getPromoId();
            $.ajax({
                method: 'GET',
                url: '/promocao/edit/' + promoId,
                beforeSend: function () {
                    // removendo as mensagens de erros passados
                    $('span').closest('.error-span').remove();
                    // remover as bordas vermelhas
                    $('.is-invalid').removeClass('is-invalid');
                    // mostra o modal
                    $('#modal-form').modal('show');
                },
                success: function (data) {
                    $('#edt_id').val(data.id);
                    $('#edt_site').text(data.site);
                    $('#edt_titulo').val(data.titulo);
                    $('#edt_descricao').val(data.descricao);
                    $('#edt_preco').val(data.preco.toLocaleString('pt-BR', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    }));
                    $('#edt_categoria').val(data.categoria.id);
                    $('#edt_linkImagem').val(data.linkImagem);
                    // mostrando imagem
                    $('#edt_imagem').attr('src', data.linkImagem);
                },
                error: function () {
                    alert('Ops.. Algum erro ocorreu, tente novamente mais tarde!');
                }
            });
            // var id = getPromoId();
            // alert('click in Edit button ' + id);
        }
    });

    // submit form to edit (modal)
    $('#btn-edit-modal').on('click', function () {
        var promo = {};
        promo.descricao = $('#edt_descricao').val();
        promo.preco = $('#edt_preco').val();
        promo.titulo = $('#edt_titulo').val();
        promo.categoria = $('#edt_categoria').val();
        promo.linkImagem = $('#edt_linkImagem').val();
        promo.id = $('#edt_id').val();

        $.ajax({
            method: 'POST',
            url: '/promocao/edit',
            data: promo,
            beforeSend: function() {
                // removendo as mensagens
                $('span').closest('.error-span').remove();
                // remover as bordas vermelhas
                $('.is-invalid').removeClass('is-invalid');
            },
            success: function () {
                $('#modal-form').modal("hide");
                table.ajax.reload();
            },
            statusCode: {
                422: function (xhr) {
                    console.log('status error: ', xhr.status);
                    var errors = $.parseJSON(xhr.responseText);
                    $.each(errors, function (key, val) {
                        $('#edt_' + key).addClass('is-invalid');
                        $('#error-' + key)
                            .addClass('invalid-feedback')
                            .append("<span class='error-span'>" + val + "</span>");
                    });
                }
            }
        })
    });


    // alterar a imagem no component <img> do modal
    $('#edit_linkImagem').on('change', function () {
        var link = $(this).val();
        $('edt_imagem').attr('src', link);
    });

    // acao de delete
    $('#btn-delete').on('click', function () {
        if (isSelectedRow()) {
            $('#modal-delete').modal('show');
            // var id = getPromoId();
            // alert('click in Edit button ' + id);
        }
    });

    // exclusão de uma promoção
    $('#btn-del-modal').on('click', function () {
        var id = getPromoId();
        $.ajax({
            method: 'GET',
            url: '/promocao/delete/' + id,
            success: function () {
                $('#modal-delete').modal('hide');
                table.ajax.reload(); // atualizar a tabela
            },
            error: function () {
                alert('Ops.. Ocorreu um erro, tente mais tarde!');
            }
        })
    });

    function getPromoId() {
        return table.row(table.$('tr.selected')).data().id;
    }

    function isSelectedRow() {
        var trow = table.row(table.$('tr.selected'));
        return trow.data() !== undefined;
    }

});
