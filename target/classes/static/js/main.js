//===============================================================
// メニュー制御用の関数とイベント設定（※バージョン2025-1）
//===============================================================
$(function(){
  //-------------------------------------------------
  // 変数の宣言
  //-------------------------------------------------
  const $menubar = $('#menubar');
  const $menubarHdr = $('#menubar_hdr');
  const breakPoint = 900;	// ここがブレイクポイント指定箇所です

  // ▼ここを切り替えるだけで 2パターンを使い分け！
  //   false → “従来どおり”
  //   true  → “ハンバーガーが非表示の間は #menubar も非表示”
  const HIDE_MENUBAR_IF_HDR_HIDDEN = false;

  // タッチデバイスかどうかの判定
  const isTouchDevice = ('ontouchstart' in window) ||
                       (navigator.maxTouchPoints > 0) ||
                       (navigator.msMaxTouchPoints > 0);

  //-------------------------------------------------
  // debounce(処理の呼び出し頻度を抑制) 関数
  //-------------------------------------------------
  function debounce(fn, wait) {
    let timerId;
    return function(...args) {
      if (timerId) {
        clearTimeout(timerId);
      }
      timerId = setTimeout(() => {
        fn.apply(this, args);
      }, wait);
    };
  }

  //-------------------------------------------------
  // ドロップダウン用の初期化関数
  //-------------------------------------------------
  function initDropdown($menu, isTouch) {
    // ドロップダウンメニューが存在するliにクラス追加
    $menu.find('ul li').each(function() {
      if ($(this).find('ul').length) {
        $(this).addClass('ddmenu_parent');
        $(this).children('a').addClass('ddmenu');
      }
    });

    // ドロップダウン開閉のイベント設定
    if (isTouch) {
      // タッチデバイスの場合 → タップで開閉
      $menu.find('.ddmenu').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const $dropdownMenu = $(this).siblings('ul');
        if ($dropdownMenu.is(':visible')) {
          $dropdownMenu.hide();
        } else {
          $menu.find('.ddmenu_parent ul').hide(); // 他を閉じる
          $dropdownMenu.show();
        }
      });
    } else {
      // PCの場合 → ホバーで開閉
      $menu.find('.ddmenu_parent').hover(
        function() {
          $(this).children('ul').show();
        },
        function() {
          $(this).children('ul').hide();
        }
      );
    }
  }

  //-------------------------------------------------
  // ハンバーガーメニューでの開閉制御関数
  //-------------------------------------------------
  function initHamburger($hamburger, $menu) {
    $hamburger.on('click', function() {
      $(this).toggleClass('ham');
      if ($(this).hasClass('ham')) {
        $menu.show();
        // ▼ ブレイクポイント未満でハンバーガーが開いたら body のスクロール禁止
        //    （メニューが画面いっぱいに fixed 表示されている時に背後をスクロールさせないため）
        if ($(window).width() < breakPoint) {
          $('body').addClass('noscroll');
        }
      } else {
        $menu.hide();
        // ▼ ハンバーガーを閉じたらスクロール禁止を解除
        if ($(window).width() < breakPoint) {
          $('body').removeClass('noscroll');
        }
      }
      // ドロップダウン部分も一旦閉じる
      $menu.find('.ddmenu_parent ul').hide();
    });
  }

  //-------------------------------------------------
  // レスポンシブ時の表示制御 (リサイズ時)
  //-------------------------------------------------
  const handleResize = debounce(function() {
    const windowWidth = $(window).width();

    // bodyクラスの制御 (small-screen / large-screen)
    if (windowWidth < breakPoint) {
      $('body').removeClass('large-screen').addClass('small-screen');
    } else {
      $('body').removeClass('small-screen').addClass('large-screen');
      // PC表示になったら、ハンバーガー解除 + メニューを開く
      $menubarHdr.removeClass('ham');
      $menubar.find('.ddmenu_parent ul').hide();

      // ▼ PC表示に切り替わったらスクロール禁止も解除しておく (保険的な意味合い)
      $('body').removeClass('noscroll'); // ★追加

      // ▼ #menubar を表示するか/しないかの切り替え
      if (HIDE_MENUBAR_IF_HDR_HIDDEN) {
        $menubarHdr.hide();
        $menubar.hide();
      } else {
        $menubarHdr.hide();
        $menubar.show();
      }
    }

    // スマホ(ブレイクポイント未満)のとき
    if (windowWidth < breakPoint) {
      $menubarHdr.show();
      if (!$menubarHdr.hasClass('ham')) {
        $menubar.hide();
        // ▼ ハンバーガーが閉じている状態ならスクロール禁止も解除
        $('body').removeClass('noscroll'); // ★追加
      }
    }
  }, 200);

  //-------------------------------------------------
  // 初期化
  //-------------------------------------------------
  // 1) ドロップダウン初期化 (#menubar)
  initDropdown($menubar, isTouchDevice);

  // 2) ハンバーガーメニュー初期化 (#menubar_hdr + #menubar)
  initHamburger($menubarHdr, $menubar);

  // 3) レスポンシブ表示の初期処理 & リサイズイベント
  handleResize();
  $(window).on('resize', handleResize);

  //-------------------------------------------------
  // アンカーリンク(#)のクリックイベント
  //-------------------------------------------------
  $menubar.find('a[href^="#"]').on('click', function() {
    // ドロップダウンメニューの親(a.ddmenu)のリンクはメニューを閉じない
    if ($(this).hasClass('ddmenu')) return;

    // スマホ表示＆ハンバーガーが開いている状態なら閉じる
    if ($menubarHdr.is(':visible') && $menubarHdr.hasClass('ham')) {
      $menubarHdr.removeClass('ham');
      $menubar.hide();
      $menubar.find('.ddmenu_parent ul').hide();
      // ハンバーガーが閉じたのでスクロール禁止を解除
      $('body').removeClass('noscroll'); // ★追加
    }
  });

  //-------------------------------------------------
  // 「header nav」など別メニューにドロップダウンだけ適用したい場合
  //-------------------------------------------------
  // 例：header nav へドロップダウンだけ適用（ハンバーガー連動なし）
  //initDropdown($('header nav'), isTouchDevice);
});


//===============================================================
// スムーススクロール（※バージョン2024-1）※通常タイプ
//===============================================================
$(function() {
    // ページ上部へ戻るボタンのセレクター
    var topButton = $('.pagetop');
    // ページトップボタン表示用のクラス名
    var scrollShow = 'pagetop-show';

    // スムーススクロールを実行する関数
    // targetにはスクロール先の要素のセレクターまたは'#'（ページトップ）を指定
    function smoothScroll(target) {
        // スクロール先の位置を計算（ページトップの場合は0、それ以外は要素の位置）
        var scrollTo = target === '#' ? 0 : $(target).offset().top;
        // アニメーションでスムーススクロールを実行
        $('html, body').animate({scrollTop: scrollTo}, 500);
    }

    // ページ内リンクとページトップへ戻るボタンにクリックイベントを設定
    $('a[href^="#"], .pagetop').click(function(e) {
        e.preventDefault(); // デフォルトのアンカー動作をキャンセル
        var id = $(this).attr('href') || '#'; // クリックされた要素のhref属性を取得、なければ'#'
        smoothScroll(id); // スムーススクロールを実行
    });

    // スクロールに応じてページトップボタンの表示/非表示を切り替え
    $(topButton).hide(); // 初期状態ではボタンを隠す
    $(window).scroll(function() {
        if($(this).scrollTop() >= 300) { // スクロール位置が300pxを超えたら
            $(topButton).fadeIn().addClass(scrollShow); // ボタンを表示
        } else {
            $(topButton).fadeOut().removeClass(scrollShow); // それ以外では非表示
        }
    });

    // ページロード時にURLのハッシュが存在する場合の処理
    if(window.location.hash) {
        // 1. まずブラウザの自動ジャンプを阻止して、トップで待機させる
        // （これを入れないと、ブラウザによってはガクッと先に移動してしまうため）
        $('html, body').scrollTop(0);
        
        // 2. 0.5秒待ってから、改めてスムーススクロール実行
        setTimeout(function() {
            smoothScroll(window.location.hash);
        }, 500);
    }
});


//===============================================================
// スライドショー
//===============================================================
$(function() {
	var slides = $('#mainimg .slide');
	var slideCount = slides.length;
	var currentIndex = 0;

	slides.eq(currentIndex).css('opacity', 1).addClass('active');

	setInterval(function() {
		var nextIndex = (currentIndex + 1) % slideCount;
		slides.eq(currentIndex).css('opacity', 0).removeClass('active');
		slides.eq(nextIndex).css('opacity', 1).addClass('active');
		currentIndex = nextIndex;
	}, 5000); // 5秒ごとにスライドを切り替える
});


//===============================================================
// Masonry + Fancybox + 動画ホバー処理（iOS強制描画版）
//===============================================================
$(function () {
  var $grid = $('.masonry-grid');

  if (!$grid.length) return;

  // iOSで頑固にサムネが出ない場合の「強制描画」処理
  $grid.find('.thumb-video').each(function () {
    var v = this;

    // 1. 属性の強制セット（これがないとJSでの操作が拒否されることがあります）
    $(v).attr({
      'muted': true,       // 必須：音ありだと自動再生とみなされブロックされる
      'playsinline': true, // 必須：全画面にならずに再生する
      'preload': 'metadata'
    });
    
    // 2. 「一瞬だけ再生して、即止める」
    // これを行うと、iOSは必ず映像データを読み込み、描画します。
    var playPromise = v.play();

    if (playPromise !== undefined) {
      playPromise.then(function() {
        // 再生成功（＝読み込み開始）直後にここに来ます
        v.pause();           // 即停止
        v.currentTime = 0.1; // 0.1秒地点を表示
        
        // 動画の高さが確定したはずなので、Masonryレイアウトを再計算
        // （これをしないと動画と次の要素が重なることがあります）
        setTimeout(function(){
            if ($grid.data('masonry')) {
                $grid.masonry('layout');
            }
        }, 100);

      }).catch(function(error) {
        // 省電力モードなどで自動再生が完全にブロックされた場合はここに来ます
        console.log('Autoplay was prevented:', error);
      });
    }
  });

  // 画像(img)の読み込み完了を待ってから Masonry 初期化
  // ※ videoタグはこのimagesLoadedでは感知されないため、上のJS内で layout を呼んでいます
  $grid.imagesLoaded(function () {
    $grid.masonry({
      itemSelector: '.grid-item',
      columnWidth: '.grid-sizer',
      percentPosition: true,
      gutter: 16
    });
  });

  // 画像が1枚ずつ読み込まれたときもレイアウト調整
  $grid.imagesLoaded().progress(function () {
    $grid.masonry('layout');
  });

  // Fancybox 初期化
  $('[data-fancybox]').fancybox({
    loop: true,
    smallBtn: true,
    toolbar: false
  });

  // マウスホバー時の挙動
  $grid.on('mouseenter', '.is-video video', function () {
    this.play();
  });

  $grid.on('mouseleave', '.is-video video', function () {
    this.pause();
  });
});


//===============================================================
// タッチ端末でのメニューのタップ操作
//===============================================================
$(function () {
  // ヘッダー要素を取得
  var $header = $('header');

  // メニュー内のリンクがクリックされた時の処理
  $('header nav a').on('click', function (e) {
    
    // 画面幅が900px以下（スマホ・タブレット想定）の場合のみ発動
    // ※テンプレートのブレークポイントに合わせて数値は調整してください
    if ($(window).width() <= 900) {
      
      // メニューがまだ開いていない（.is-openが無い）場合
      if (!$header.hasClass('is-open')) {
        // 1. 本来のリンク移動をキャンセル（無効化）
        e.preventDefault();
        
        // 2. メニューにクラスをつけて広げる
        $header.addClass('is-open');
        
        // ここで処理終了（リンクには飛ばない）
        return; 
      }
      
      // メニューが既に開いている場合は、何もしないので
      // そのままリンク先に移動します。
    }
  });

  // 【おまけ】メニュー以外の場所をタップしたらメニューを閉じる処理
  // これがないと開きっぱなしになって邪魔になるため
  $(document).on('click', function (e) {
    // クリックされた場所がヘッダーの中じゃなければ
    if (!$(e.target).closest('header').length) {
      $header.removeClass('is-open');
    }
  });
});
